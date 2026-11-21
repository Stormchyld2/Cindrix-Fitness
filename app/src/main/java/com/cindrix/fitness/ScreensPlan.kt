package com.cindrix.fitness

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate

private fun dayKeyFor(d: DayOfWeek, keys: List<String>): String? =
    ALL_DAYS.getOrNull(d.value - 1)?.takeIf { it in keys }

private fun nextDayKey(d: DayOfWeek, keys: List<String>): String {
    val idx = d.value - 1
    return (1..7).map { (idx + it) % 7 }.mapNotNull { ALL_DAYS.getOrNull(it) }.firstOrNull { it in keys } ?: keys.first()
}

private fun strengthCount(items: List<PlanItem>): Int =
    items.count { Library.get(it.id)?.timed == null }

@Composable
fun TodayScreen(st: AppState, nav: Nav) {
    val dow = LocalDate.now().dayOfWeek
    val keys = st.dayKeys()
    val todayKey = dayKeyFor(dow, keys)
    val key = todayKey ?: nextDayKey(dow, keys)
    val wkStart = weekStart(LocalDate.now()).toString()
    Page {
        Header(
            "Today", "Discipline today. A stronger tomorrow.",
            action = {
                Box(Modifier.size(44.dp).clip(CircleShape).clickable { nav.go(Screen.Settings) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = C.mut)
                }
            },
        )
        CCard(onClick = { nav.go(Screen.Rewards) }) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(C.line), contentAlignment = Alignment.Center) {
                    Text("${st.level()}", color = C.acc, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
                Column(Modifier.weight(1f)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${st.rank()} · Level ${st.level()}", color = C.txt, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text("${st.xpIntoLevel()} / 1,000 XP", color = C.mut, fontSize = 12.sp)
                    }
                    Box(Modifier.padding(top = 6.dp)) { Bar(st.xpIntoLevel() / 1000f) }
                }
            }
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("Week streak ${st.streakWeeks()}")
                Pill("Week ${st.programWeek()} of 12", C.mut)
            }
        }
        val items = st.plan[key].orEmpty()
        CCard {
            val first = items.firstOrNull { Library.get(it.id)?.timed == null }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (first != null) ExercisePhoto(first.id, Modifier.width(96.dp).aspectRatio(1.5f), compact = true)
                Column {
                    Text(
                        if (todayKey != null) "Today · ${DAY_SHORT[key]}" else "Rest day · next is ${DAY_SHORT[key]}",
                        color = C.mut, fontSize = 12.sp,
                    )
                    Text(DAY_TITLES.getValue(key).substringAfter("· "), color = C.txt, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Text("${strengthCount(items)} strength moves plus warm-up, slam ball and boxing", color = C.mut, fontSize = 12.sp)
                }
            }
            Box(Modifier.padding(top = 10.dp)) {
                PrimaryButton(if (todayKey != null) "Start Workout" else "Preview next workout") {
                    nav.go(if (todayKey != null) Screen.Active(key) else Screen.WorkoutDay(key))
                }
            }
        }
        CCard {
            Text("This week", color = C.txt, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                labels.forEachIndexed { i, d ->
                    val planned = keys.contains(d)
                    val done = planned && st.workouts.any { it.day == d && weekKey(it.date) == wkStart }
                    val isToday = dow.value == i + 1
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                            .background(if (done) C.line else C.bg)
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(d, color = if (isToday) C.acc else C.mut, fontSize = 11.sp, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal)
                        Text(if (done) "✓" else if (planned) "•" else "–", color = if (done) C.green else C.txt, fontSize = 15.sp)
                    }
                }
            }
        }
        CCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Weekly quests", color = C.txt, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("+150 XP when all done", color = C.mut, fontSize = 12.sp)
            }
            Quest("${st.workoutTarget()} workouts", st.workoutsThisWeek(), st.workoutTarget())
            Quest("Protein target, 5 days", st.proteinDaysThisWeek(), 5)
            Quest("Walking target, 4 days", st.stepDaysThisWeek(), 4)
        }
        val nut = st.todayEntry("nutrition")
        CCard(onClick = { nav.go(Screen.Nutrition) }) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Nutrition today", color = C.txt, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text("Log meals ›", color = C.acc, fontSize = 13.sp)
            }
            Muted("${fmt(nut["calories"] ?: 0.0)} / ${st.goals.calories} cal · ${fmt(nut["protein"] ?: 0.0)} / ${st.goals.protein} g protein")
            Bar(((nut["protein"] ?: 0.0) / st.goals.protein).toFloat())
        }
    }
}

@Composable
private fun Quest(label: String, have: Int, need: Int) {
    Column(Modifier.padding(top = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = C.txt, fontSize = 13.sp)
            Text("${minOf(have, need)} of $need", color = C.mut, fontSize = 13.sp)
        }
        Box(Modifier.padding(top = 4.dp)) { Bar(have.toFloat() / need) }
    }
}

@Composable
fun ProgramScreen(st: AppState, nav: Nav) {
    val mode = when (st.plan) {
        Presets.varied -> "Varied days"
        Presets.guide -> "Guide as written"
        else -> "Custom"
    }
    Page {
        Header("Program", "Hybrid Muscle, Fat Loss and Boxing · week ${st.programWeek()} of 12")
        CCard {
            Text("Training days per week", color = C.txt, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Muted("3 days is your Hybrid guide. 5 days adds two tactical fitness days (Tuesday and Thursday).")
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("3 days", st.daysPerWeek == 3, Modifier.weight(1f)) { st.setDaysPerWeek(3) }
                Chip("5 days", st.daysPerWeek == 5, Modifier.weight(1f)) { st.setDaysPerWeek(5) }
            }
        }
        CCard {
            Text("Plan style: $mode", color = C.txt, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Muted("Varied days keeps every strength move different across the three days by using your guide's own substitute movements on Friday. You can change any exercise.")
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlineButton("Varied days", Modifier.weight(1f)) { st.usePreset(true) }
                OutlineButton("Guide as written", Modifier.weight(1f)) { st.usePreset(false) }
            }
        }
        st.dayKeys().forEach { d ->
            val items = st.plan[d].orEmpty()
            CCard(onClick = { nav.go(Screen.DayEditor(d)) }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val first = items.firstOrNull { Library.get(it.id)?.timed == null }
                    if (first != null) ExercisePhoto(first.id, Modifier.width(96.dp).aspectRatio(1.5f), compact = true)
                    Column(Modifier.weight(1f)) {
                        Text(d.let { DAY_TITLES.getValue(it).substringBefore(" ·").uppercase() }, color = C.acc, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(DAY_TITLES.getValue(d).substringAfter("· "), color = C.txt, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                        Muted("${strengthCount(items)} strength moves · tap to edit")
                    }
                    Text("›", color = C.mut, fontSize = 24.sp)
                }
            }
        }
        CCard {
            Text("Boxing and slam-ball progression", color = C.txt, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Muted("Weeks 1–4: 4 boxing rounds, 30 s rest; slam ball 2 rounds.")
            Muted("Weeks 5–8: boxing rest down to 20 s.")
            Muted("Weeks 9–12: add a 5th round, Friday first.")
        }
        Muted("This app follows your Hybrid guide. It is educational and not a substitute for medical care or physical therapy.", 12)
    }
}

@Composable
fun DayEditorScreen(st: AppState, nav: Nav, day: String) {
    val items = st.plan[day].orEmpty()
    var editing by remember { mutableIntStateOf(-1) }
    Page {
        Header(DAY_TITLES.getValue(day).substringBefore(" ·"), DAY_TITLES.getValue(day).substringAfter("· "), onBack = { nav.back() })
        items.forEachIndexed { i, p ->
            val ex = Library.get(p.id) ?: return@forEachIndexed
            CCard(pad = 8.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExercisePhoto(ex.id, Modifier.width(66.dp).aspectRatio(1.5f).clickable { nav.openEx(ex.id) }, compact = true)
                    Column(Modifier.weight(1f).clickable(enabled = ex.timed == null) { editing = i }) {
                        Text(ex.name, color = C.txt, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            if (ex.timed != null) "${ex.timed.rounds} round(s)" else "${p.sets} × ${if (p.lo == p.hi) "${p.lo}" else "${p.lo}–${p.hi}"}${if (ex.perSide) " each side" else ""}",
                            color = C.mut, fontSize = 12.sp,
                        )
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.End) {
                    SmallAction("▲", i > 0) { st.setPlan(day, items.toMutableList().also { l -> l.add(i - 1, l.removeAt(i)) }) }
                    SmallAction("▼", i < items.lastIndex) { st.setPlan(day, items.toMutableList().also { l -> l.add(i + 1, l.removeAt(i)) }) }
                    SmallAction("Swap", true) { nav.go(Screen.Picker(day, i)) }
                    SmallAction("Remove", true) { st.setPlan(day, items.toMutableList().also { l -> l.removeAt(i) }) }
                }
            }
        }
        PrimaryButton("Add exercise") { nav.go(Screen.Picker(day, null)) }
        OutlineButton("Reset this day to the guide", Modifier.fillMaxWidth()) { st.setPlan(day, Presets.guide.getValue(day)) }
    }
    val idx = editing
    if (idx in items.indices) {
        val p = items[idx]
        var sets by remember(idx) { mutableIntStateOf(p.sets) }
        var lo by remember(idx) { mutableIntStateOf(p.lo) }
        var hi by remember(idx) { mutableIntStateOf(p.hi) }
        AlertDialog(
            onDismissRequest = { editing = -1 },
            containerColor = C.surf,
            title = { Text(Library.get(p.id)?.name ?: "", color = C.txt, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StepRow("Sets", sets, 1, 8) { sets = it }
                    StepRow("Reps from", lo, 1, 30) { lo = it; if (hi < lo) hi = lo }
                    StepRow("Reps to", hi, 1, 30) { hi = it; if (lo > hi) lo = hi }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    st.setPlan(day, items.toMutableList().also { l -> l[idx] = PlanItem(p.id, sets, lo, hi) })
                    editing = -1
                }) { Text("Save", color = C.acc) }
            },
            dismissButton = { TextButton(onClick = { editing = -1 }) { Text("Cancel", color = C.mut) } },
        )
    }
}

@Composable
fun SmallAction(text: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.height(44.dp).clip(RoundedCornerShape(8.dp)).clickable(enabled = enabled, onClick = onClick).padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, color = if (enabled) C.acc else C.line, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun StepRow(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = C.txt, fontSize = 15.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Chip("−", false, Modifier.width(48.dp)) { if (value > min) onChange(value - 1) }
            Text("$value", color = C.txt, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, modifier = Modifier.width(48.dp))
            Chip("+", false, Modifier.width(48.dp)) { if (value < max) onChange(value + 1) }
        }
    }
}

@Composable
fun PickerScreen(st: AppState, nav: Nav, day: String, replace: Int?) {
    var query by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("All") }
    val list = Library.all.filter {
        (group == "All" || it.group == group) && it.name.contains(query, ignoreCase = true) && it.timed == null
    }
    Column(Modifier.fillMaxSize().background(C.bg).padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Header(if (replace != null) "Swap exercise" else "Add exercise", "Tap an exercise to choose it", onBack = { nav.back() })
        OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Search") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        GroupChips(group) { group = it }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(list) { ex ->
                CCard(pad = 8.dp, onClick = {
                    val cur = st.plan[day].orEmpty().toMutableList()
                    val p = PlanItem(ex.id, ex.sets, ex.repsLo, ex.repsHi)
                    if (replace != null && replace in cur.indices) {
                        cur[replace] = p
                    } else {
                        val cool = cur.indexOfFirst { it.id == "cooldown" }
                        val at = if (cool >= 0) cool else cur.size
                        cur.add(at, p)
                    }
                    st.setPlan(day, cur)
                    nav.back()
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ExercisePhoto(ex.id, Modifier.width(66.dp).aspectRatio(1.5f), compact = true)
                        Column(Modifier.weight(1f)) {
                            Text(ex.name, color = C.txt, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(ex.group + if (!ex.inGuide) " · not in your guide" else "", color = C.mut, fontSize = 12.sp)
                        }
                        SmallAction("How-to", true) { nav.openEx(ex.id) }
                    }
                }
            }
        }
    }
}

@Composable
fun GroupChips(selected: String, onPick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Library.groups.take(4).forEach { Chip(it, selected == it, Modifier.weight(1f)) { onPick(it) } }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Library.groups.drop(4).forEach { Chip(it, selected == it, Modifier.weight(1f)) { onPick(it) } }
        }
    }
}

@Composable
fun WorkoutsScreen(st: AppState, nav: Nav) {
    Page {
        Header("Workouts", "Pick a day, or open the round timer")
        st.dayKeys().forEach { d ->
            val items = st.plan[d].orEmpty()
            CCard(onClick = { nav.go(Screen.WorkoutDay(d)) }) {
                Text(DAY_TITLES.getValue(d).substringBefore(" ·").uppercase(), color = C.acc, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(DAY_TITLES.getValue(d).substringAfter("· "), color = C.txt, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                Muted("${items.size} items · done ${st.workouts.count { it.day == d }} times")
            }
        }
        CCard(onClick = { nav.go(Screen.TimerHub) }) {
            Text("Round timer", color = C.txt, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Muted("Heavy bag, boxing machine and slam-ball rounds with work and rest countdowns.")
        }
        CCard(onClick = { nav.go(Screen.WorkoutHistory) }) {
            Text("Workout log", color = C.txt, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Muted("${st.workouts.size} workouts and ${st.setLogs.size} sets logged")
        }
    }
}

@Composable
fun WorkoutDayScreen(st: AppState, nav: Nav, day: String) {
    val items = st.plan[day].orEmpty()
    Page {
        Header(DAY_TITLES.getValue(day).substringBefore(" ·"), DAY_TITLES.getValue(day).substringAfter("· ") + " · tap a move for how-to", onBack = { nav.back() })
        items.forEach { p ->
            val ex = Library.get(p.id) ?: return@forEach
            CCard(pad = 6.dp, onClick = { nav.openEx(ex.id) }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ExercisePhoto(ex.id, Modifier.width(72.dp).aspectRatio(1.5f), compact = true)
                    Column(Modifier.weight(1f)) {
                        Text(ex.name, color = C.txt, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            if (ex.timed != null) "${ex.timed.rounds} round(s) · ${ex.timed.workSec / 60}:${"%02d".format(ex.timed.workSec % 60)}"
                            else "${p.sets} × ${if (p.lo == p.hi) "${p.lo}" else "${p.lo}–${p.hi}"} · ${st.bandFor(ex).takeIf { ex.startLb > 0 }?.let { "$it lb" } ?: "bodyweight"}",
                            color = C.mut, fontSize = 12.sp,
                        )
                    }
                    Text("›", color = C.mut, fontSize = 22.sp)
                }
            }
        }
        PrimaryButton("Start Workout") { nav.go(Screen.Active(day)) }
    }
}

@Composable
fun LibraryScreen(nav: Nav) {
    var query by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("All") }
    val list = Library.all.filter { (group == "All" || it.group == group) && it.name.contains(query, ignoreCase = true) }
    Column(Modifier.fillMaxSize().background(C.bg).padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Header("Library", "${Library.all.size} exercises · tap for how-to")
        OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Search exercises") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        GroupChips(group) { group = it }
        LazyVerticalGrid(
            modifier = Modifier.weight(1f), columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(list) { ex ->
                CCard(pad = 6.dp, onClick = { nav.openEx(ex.id) }) {
                    ExercisePhoto(ex.id, Modifier.fillMaxWidth().aspectRatio(1.5f), compact = true)
                    Text(ex.name, color = C.txt, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
                    Text(ex.group + if (!ex.inGuide) " · extra" else "", color = C.mut, fontSize = 11.sp)
                }
            }
        }
    }
}
