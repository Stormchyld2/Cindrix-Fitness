package com.cindrix.fitness

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val SLOTS = listOf("Breakfast", "Lunch", "Dinner", "Snack")
val WEEK = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

private fun slotType(slot: String) = when (slot) {
    "Breakfast" -> "Breakfast"
    "Snack" -> "Snack"
    else -> "Main"
}

private fun weekLabel(offset: Int) = (if (offset == 0) "This week" else "Next week")

@Composable
private fun WeekChips(st: AppState) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(0, 1).forEach { o ->
            Chip(weekLabel(o), st.weekOffset == o, Modifier.weight(1f)) { st.weekOffset = o }
        }
    }
}

@Composable
private fun FlagPill(r: Recipe) {
    if (r.flags.isNotEmpty()) Pill("Check: " + r.flags.joinToString(", "), C.yellow)
}

@Composable
fun MealPlannerScreen(st: AppState, nav: Nav) {
    val wk = st.viewWeek()
    val menu = st.menuFor(wk)
    var day by remember { mutableStateOf(WEEK[java.time.LocalDate.now().dayOfWeek.value - 1]) }
    var picking by remember { mutableStateOf<String?>(null) }
    val favCounts = RECIPE_TYPES.associateWith { t -> Recipes.ofType(t).count { it.id in st.favorites } }
    val missing = RECIPE_TYPES.filter { favCounts.getValue(it) == 0 }
    Page {
        Header("Meal planner", "Pick favorites, build the week, export groceries", onBack = { nav.back() })
        WeekChips(st)
        CCard {
            Text("1. Pick your favorites", color = C.txt, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            Muted("A week has 7 breakfasts, 14 lunches and dinners, and 7 snacks. You do not need that many favorites. Tick enough that the menu has variety and only a few repeat.", 12)
            RECIPE_TYPES.forEach { t ->
                val (lo, target, hi) = pickGuide(t)
                val n = favCounts.getValue(t)
                Column(Modifier.padding(top = 8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(typeLabel(t), color = C.txt, fontSize = 13.sp)
                        Text("$n picked · aim for $lo–$hi (about $target)", color = if (n >= lo) C.green else C.mut, fontSize = 12.sp)
                    }
                    Bar(n.toFloat() / target)
                }
            }
            Row(Modifier.padding(top = 10.dp)) { PrimaryButton("Browse recipes and tick favorites", Modifier.fillMaxWidth()) { nav.go(Screen.Recipes) } }
        }
        CCard {
            Text("2. Build the menu", color = C.txt, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            StepRow("Cooking for", st.people, 1, 10) { st.choosePeople(it) }
            Muted("Recipes are scaled to this many people on the grocery list.", 12)
            if (missing.isNotEmpty()) Muted("Pick at least one favorite in: ${missing.joinToString { typeLabel(it) }}.", 12)
            Row(Modifier.padding(top = 8.dp)) {
                PrimaryButton(if (menu.isEmpty()) "Build menu for ${weekLabel(st.weekOffset).lowercase()}" else "Rebuild with new picks", Modifier.fillMaxWidth(), enabled = favCounts.values.any { it > 0 }) { st.buildMenu(wk) }
            }
        }
        if (menu.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                WEEK.forEach { d -> Chip(d, day == d, Modifier.weight(1f)) { day = d } }
            }
            SLOTS.forEach { slot ->
                val r = menu["$day|$slot"]?.let { Recipes.byId[it] }
                CCard(onClick = { picking = slot }) {
                    Text(slot.uppercase(), color = C.acc, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (r == null) Muted("Empty. Tap to choose from your favorites.") else {
                        Text(r.title, color = C.txt, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Muted("${r.cuisine}${r.minutes?.let { " · $it min" } ?: ""} · serves ${r.servings}", 12)
                        FlagPill(r)
                    }
                }
                if (r != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlineButton("View recipe", Modifier.weight(1f)) { nav.go(Screen.Recipe(r.id)) }
                        val nu = r.nutrition
                        if (nu != null) {
                            OutlineButton("Log 1 serving", Modifier.weight(1f)) { st.addNutrition(nu[0], nu[1], 0.0) }
                        }
                    }
                }
            }
            val flagged = menu.values.mapNotNull { Recipes.byId[it] }.filter { it.flags.isNotEmpty() }.map { it.title }.toSet()
            if (flagged.isNotEmpty()) {
                CCard {
                    Text("Check ingredients", color = C.yellow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Muted("${flagged.size} planned recipes contain an ingredient from your exclusion list (or a label to check). Review them before shopping.", 12)
                }
            }
            CCard {
                Text("3. Grocery list", color = C.txt, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                Muted("${Grocery.build(menu.values.toList(), st.people).size} items for ${weekLabel(st.weekOffset).lowercase()}")
                Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimaryButton("Open grocery list", Modifier.weight(1f)) { nav.go(Screen.Shopping) }
                    OutlineButton("Clear this week", Modifier.weight(1f), C.mut) { st.clearMenu(wk) }
                }
            }
            Muted("Calories and protein are only shown for the 63 snack recipes, and those are provisional estimates. The other recipes have no nutrition data yet.", 12)
        }
    }
    val slot = picking
    if (slot != null) {
        val favs = Recipes.ofType(slotType(slot)).filter { it.id in st.favorites }
        AlertDialog(
            onDismissRequest = { picking = null },
            containerColor = C.surf,
            title = { Text("$day · $slot", color = C.txt, fontSize = 18.sp) },
            text = {
                Column(Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (favs.isEmpty()) Muted("No favorites in this category yet.")
                    favs.forEach { r ->
                        CCard(pad = 10.dp, onClick = { st.setSlot(wk, "$day|$slot", r.id); picking = null }) {
                            Text(r.title, color = C.txt, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Muted(r.cuisine, 12)
                        }
                    }
                    OutlineButton("Leave empty", Modifier.fillMaxWidth(), C.mut) { st.setSlot(wk, "$day|$slot", null); picking = null }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { picking = null }) { Text("Cancel", color = C.mut) } },
        )
    }
}

@Composable
fun RecipesScreen(st: AppState, nav: Nav) {
    var type by remember { mutableStateOf("Breakfast") }
    var query by remember { mutableStateOf("") }
    var likedOnly by remember { mutableStateOf(false) }
    var snackGroup by remember { mutableStateOf("All") }
    val (lo, target, hi) = pickGuide(type)
    val liked = Recipes.ofType(type).count { it.id in st.favorites }
    val groups = listOf("All") + Recipes.ofType("Snack").mapNotNull { it.group }.distinct()
    val list = Recipes.ofType(type).filter {
        (query.isBlank() || it.title.contains(query, true) || it.cuisine.contains(query, true)) &&
            (!likedOnly || it.id in st.favorites) &&
            (type != "Snack" || snackGroup == "All" || it.group == snackGroup)
    }
    Column(Modifier.fillMaxSize().background(C.bg).padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Header("Recipes", "${Recipes.all.size} recipes · tap a title to read it", onBack = { nav.back() })
        CCard(pad = 10.dp) {
            Text("${typeLabel(type)}: $liked picked", color = C.txt, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Muted("Pick about $target (anywhere from $lo to $hi) so your week has variety. Tick the ones you would actually eat.", 12)
            Bar(liked.toFloat() / target)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            RECIPE_TYPES.forEach { t -> Chip(typeLabel(t), type == t, Modifier.weight(1f)) { type = t } }
        }
        if (type == "Snack") {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.verticalScroll(rememberScrollState()).heightIn(max = 96.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    groups.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { g -> Chip(g, snackGroup == g, Modifier.weight(1f)) { snackGroup = g } }
                            repeat(3 - row.size) { androidx.compose.foundation.layout.Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Search") }, singleLine = true, modifier = Modifier.weight(1f))
            Chip("Picked", likedOnly, Modifier.width(84.dp)) { likedOnly = !likedOnly }
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(list, key = { it.id }) { r ->
                val on = r.id in st.favorites
                CCard(pad = 10.dp, selected = on) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(Modifier.weight(1f).clickable { nav.go(Screen.Recipe(r.id)) }) {
                            Text(r.title, color = C.txt, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Muted("${r.cuisine}${r.minutes?.let { " · $it min" } ?: ""} · serves ${r.servings}", 12)
                            FlagPill(r)
                        }
                        Chip(if (on) "✓ Picked" else "Pick", on, Modifier.width(88.dp)) { st.toggleFav(r.id) }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeScreen(st: AppState, nav: Nav, id: String) {
    val r = Recipes.byId[id]
    Page {
        if (r == null) {
            Header("Recipe", "Not found", onBack = { nav.back() })
            return@Page
        }
        Header(r.title, "${r.cuisine}${r.minutes?.let { " · $it min" } ?: ""} · serves ${r.servings}", onBack = { nav.back() })
        val on = r.id in st.favorites
        Chip(if (on) "✓ Picked as a favorite" else "Pick as a favorite", on, Modifier.fillMaxWidth()) { st.toggleFav(r.id) }
        if (r.flags.isNotEmpty()) {
            CCard {
                Text("Check before cooking", color = C.yellow, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Muted("This recipe lists or mentions: ${r.flags.joinToString(", ")}. It is on your exclusion list, or the label needs checking. Substitute or skip it.", 12)
            }
        }
        CCard {
            Text("Ingredients", color = C.txt, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            r.ingredients.forEach { Muted("• $it", 13) }
        }
        CCard {
            Text("Steps", color = C.txt, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            r.steps.forEachIndexed { i, step ->
                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${i + 1}", color = C.acc, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    Text(step, color = C.txt, fontSize = 13.sp, lineHeight = 19.sp)
                }
            }
        }
        r.nutrition?.let { n ->
            CCard {
                Text("Per serving (provisional estimate)", color = C.txt, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Muted("${fmt(n[0])} cal · ${fmt(n[1])} g protein · ${fmt(n[2])} g carbs · ${fmt(n[3])} g fat · ${fmt(n[4])} g fiber", 13)
                Muted("Computed from generic values, not lab or USDA verified. Brands vary.", 12)
            }
        }
        Muted(
            when (r.origin) {
                "orig" -> "From your original recipe files."
                else -> "A newly written starting formulation. Kitchen-test quantities and times, and check packaged ingredients."
            },
            12,
        )
    }
}

@Composable
fun ShoppingScreen(st: AppState, nav: Nav) {
    val wk = st.viewWeek()
    val ids = st.menuFor(wk).values.toList()
    val items = Grocery.build(ids, st.people)
    val ctx = LocalContext.current
    val clip = LocalClipboardManager.current
    val prefix = "$wk|"
    val left = items.filter { prefix + it.key !in st.shopChecked }
    val title = "Cindrix Fitness grocery list, week of $wk (for ${st.people})"
    Page {
        Header("Grocery list", "${left.size} left to buy of ${items.size}", onBack = { nav.back() })
        WeekChips(st)
        StepRow("Cooking for", st.people, 1, 10) { st.choosePeople(it) }
        if (items.isEmpty()) {
            CCard {
                Muted("No menu for ${weekLabel(st.weekOffset).lowercase()} yet. Build one in the meal planner and the list appears here.")
                Row(Modifier.padding(top = 8.dp)) { PrimaryButton("Open meal planner", Modifier.fillMaxWidth()) { nav.back(); nav.go(Screen.MealPlanner) } }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryButton("Share list", Modifier.weight(1f)) {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, title)
                        putExtra(Intent.EXTRA_TEXT, Grocery.asText(title, left))
                    }
                    ctx.startActivity(Intent.createChooser(send, "Share grocery list"))
                }
                OutlineButton("Copy", Modifier.weight(1f)) { clip.setText(AnnotatedString(Grocery.asText(title, left))) }
            }
            Muted("Sharing sends only what is still unchecked. Quantities are summed across recipes and rounded up; check them against what you already have.", 12)
            items.groupBy { it.group }.forEach { (g, l) ->
                CCard {
                    Text(g, color = C.acc, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    l.forEach { it2 ->
                        val on = prefix + it2.key in st.shopChecked
                        Chip(
                            (if (on) "✓ " else "") + (if (it2.qty.isNotEmpty()) it2.qty + "  " else "") + it2.name,
                            on, Modifier.fillMaxWidth().padding(top = 4.dp),
                        ) { st.toggleShop(prefix + it2.key) }
                    }
                }
            }
            OutlineButton("Uncheck everything", Modifier.fillMaxWidth(), C.mut) { st.clearChecks(prefix) }
        }
    }
}
