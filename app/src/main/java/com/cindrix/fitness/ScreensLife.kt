package com.cindrix.fitness

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NutritionScreen(st: AppState, nav: Nav) {
    val n = st.todayEntry("nutrition")
    var cal by remember { mutableStateOf("") }
    var pro by remember { mutableStateOf("") }
    var wat by remember { mutableStateOf("") }
    Page {
        Header("Nutrition", "Today's totals against your targets", onBack = { nav.back() })
        CCard {
            MacroRow("Calories", n["calories"] ?: 0.0, st.goals.calories.toDouble(), "")
            MacroRow("Protein", n["protein"] ?: 0.0, st.goals.protein.toDouble(), " g")
            MacroRow("Water", n["water"] ?: 0.0, st.goals.water.toDouble(), " oz")
        }
        CCard {
            Text("Add to today", color = C.txt, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NumField("Calories", cal, Modifier.weight(1f)) { cal = it }
                NumField("Protein g", pro, Modifier.weight(1f)) { pro = it }
                NumField("Water oz", wat, Modifier.weight(1f)) { wat = it }
            }
            Row(Modifier.padding(top = 8.dp)) {
                PrimaryButton("Add", Modifier.fillMaxWidth()) {
                    val c = cal.toDoubleOrNull() ?: 0.0
                    val p = pro.toDoubleOrNull() ?: 0.0
                    val w = wat.toDoubleOrNull() ?: 0.0
                    if (c + p + w > 0) {
                        st.addNutrition(c, p, w)
                        cal = ""; pro = ""; wat = ""
                    }
                }
            }
            OutlineButton("Reset today's totals", Modifier.fillMaxWidth().padding(top = 6.dp)) {
                st.saveEntry("nutrition", mapOf("calories" to 0.0, "protein" to 0.0, "water" to 0.0))
            }
        }
        NavCard("Mediterranean eating guide", "What to eat, a plate model and protein sources") { nav.go(Screen.Mediterranean) }
        NavCard("Shopping list", "Mediterranean staples to check off") { nav.go(Screen.Shopping) }
        Muted("Targets are your guide's defaults (about 2,400 calories, 190 g protein). Change them in Settings. General education, not medical or dietitian advice.", 12)
    }
}

@Composable
private fun MacroRow(label: String, v: Double, goal: Double, unit: String) {
    androidx.compose.foundation.layout.Column(Modifier.padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = C.txt, fontSize = 14.sp)
            Text("${fmt(v)} / ${fmt(goal)}$unit", color = C.mut, fontSize = 13.sp)
        }
        Bar((v / goal).toFloat())
    }
}

@Composable
private fun NumField(label: String, value: String, modifier: Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label, fontSize = 11.sp) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = modifier,
    )
}

@Composable
fun MediterraneanScreen(nav: Nav) {
    Page {
        Header("Mediterranean eating", "A pattern, not a strict diet", onBack = { nav.back() })
        CCard {
            Text("The idea", color = C.txt, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            Muted("Mostly plants, olive oil as the main fat, fish and seafood often, modest poultry, eggs and dairy, red meat rarely, and little sugar or processed food. It fits a high-protein muscle and fat-loss plan when you build meals around lean protein.")
        }
        CCard {
            Text("Build your plate", color = C.txt, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            listOf(
                "Half the plate: vegetables and fruit, any colors.",
                "A quarter: lean protein (fish, chicken, eggs, Greek yogurt, beans).",
                "A quarter: whole grains or starchy foods (oats, brown rice, whole-grain bread, potatoes).",
                "A thumb of olive oil or a small handful of nuts.",
                "Herbs, lemon, garlic and spices for flavor.",
            ).forEach { Muted("• $it") }
        }
        CCard {
            Text("Protein sources that fit", color = C.txt, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            Muted("Fish and seafood (salmon, sardines, tuna, shrimp), chicken and turkey, eggs, Greek yogurt and cottage cheese, lentils, chickpeas and beans, tofu. Spread protein across 3–4 meals to reach your daily target.")
        }
        CCard {
            Text("Easy swaps", color = C.txt, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            listOf(
                "Butter → olive oil.",
                "Chips → nuts or hummus with vegetables.",
                "Sugary drinks → water, sparkling water or unsweetened tea.",
                "Refined bread and pasta → whole-grain versions.",
                "Dessert most days → fruit, with sweets as an occasional treat.",
            ).forEach { Muted("• $it") }
        }
        CCard {
            Text("Sample day", color = C.txt, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            Muted("Breakfast: Greek yogurt, berries, oats and walnuts.")
            Muted("Lunch: chicken, chickpea and vegetable salad with olive oil and lemon.")
            Muted("Snack: cottage cheese with fruit, or hummus with vegetables.")
            Muted("Dinner: baked salmon, roasted vegetables, brown rice.")
            Muted("Log it in the tracker and check calories and protein against your targets.", 12)
        }
        Muted("General education, not medical or dietitian advice. Talk to a clinician about allergies, conditions or medications.", 12)
    }
}

private val SHOPPING = listOf(
    "Produce" to listOf("Leafy greens", "Tomatoes", "Cucumbers", "Peppers", "Onions and garlic", "Lemons", "Berries", "Apples or oranges"),
    "Protein" to listOf("Salmon or other fish", "Canned tuna or sardines", "Chicken breast", "Eggs", "Greek yogurt", "Cottage cheese"),
    "Pantry" to listOf("Extra-virgin olive oil", "Oats", "Brown rice", "Whole-grain bread or pasta", "Lentils", "Chickpeas", "Canned beans"),
    "Extras" to listOf("Almonds and walnuts", "Hummus", "Herbs and spices", "Feta or parmesan"),
)

@Composable
fun ShoppingScreen(st: AppState, nav: Nav) {
    Page {
        Header("Shopping list", "Mediterranean staples", onBack = { nav.back() })
        SHOPPING.forEach { (cat, list) ->
            CCard {
                Text(cat, color = C.acc, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                list.forEach { it2 ->
                    val on = it2 in st.shopChecked
                    Chip((if (on) "✓ " else "") + it2, on, Modifier.fillMaxWidth().padding(top = 4.dp)) { st.toggleShop(it2) }
                }
            }
        }
    }
}

@Composable
fun RewardsScreen(st: AppState, nav: Nav) {
    Page {
        Header("Rewards", "${st.rank()} · level ${st.level()}", onBack = { nav.back() })
        CCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${st.xp} XP total", color = C.txt, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(st.nextRank(), color = C.mut, fontSize = 12.sp)
            }
            androidx.compose.foundation.layout.Box(Modifier.padding(top = 6.dp)) { Bar(st.xpIntoLevel() / 1000f) }
            Muted("+10 per clean set, +100 per workout, +50 per band-up, +150 for a full weekly quest set.", 12)
        }
        SectionTitle("Badges")
        st.badges().forEach { b ->
            CCard(pad = 10.dp, selected = b.earned) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(b.name, color = if (b.earned) C.txt else C.mut, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(if (b.earned) "Earned" else "Locked", color = if (b.earned) C.green else C.mut, fontSize = 12.sp)
                }
                Muted(b.how, 12)
            }
        }
    }
}

@Composable
fun SettingsScreen(st: AppState, nav: Nav) {
    var cal by remember { mutableStateOf(st.goals.calories.toString()) }
    var pro by remember { mutableStateOf(st.goals.protein.toString()) }
    var wat by remember { mutableStateOf(st.goals.water.toString()) }
    var stp by remember { mutableStateOf(st.goals.steps.toString()) }
    var confirm by remember { mutableStateOf(false) }
    Page {
        Header("Settings", "Targets and data", onBack = { nav.back() })
        CCard {
            Text("Daily targets", color = C.txt, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NumField("Calories", cal, Modifier.weight(1f)) { cal = it }
                NumField("Protein g", pro, Modifier.weight(1f)) { pro = it }
            }
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NumField("Water oz", wat, Modifier.weight(1f)) { wat = it }
                NumField("Steps", stp, Modifier.weight(1f)) { stp = it }
            }
            Row(Modifier.padding(top = 8.dp)) {
                PrimaryButton("Save targets", Modifier.fillMaxWidth()) {
                    val d = st.goals
                    st.updateGoals(
                        Goals(
                            cal.toDoubleOrNull()?.toInt()?.takeIf { it > 0 } ?: d.calories,
                            pro.toDoubleOrNull()?.toInt()?.takeIf { it > 0 } ?: d.protein,
                            wat.toDoubleOrNull()?.toInt()?.takeIf { it > 0 } ?: d.water,
                            stp.toDoubleOrNull()?.toInt()?.takeIf { it > 0 } ?: d.steps,
                        ),
                    )
                }
            }
        }
        CCard {
            Text("Program", color = C.txt, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Muted("Week ${st.programWeek()} of 12 · started ${st.startDate}")
            Row(Modifier.padding(top = 6.dp)) { OutlineButton("Restart at week 1", Modifier.fillMaxWidth()) { st.resetStart() } }
        }
        CCard {
            Text("Reset everything", color = C.red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Muted("Clears workouts, trackers, XP and your plan. Cannot be undone.")
            Row(Modifier.padding(top = 6.dp)) { OutlineButton("Reset all data", Modifier.fillMaxWidth(), C.red) { confirm = true } }
        }
        Muted("Cindrix Fitness 0.1.0 · works offline · data stays on this phone. Educational, not medical advice.", 12)
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            containerColor = C.surf,
            title = { Text("Reset all data?", color = C.txt) },
            text = { Text("This clears everything and cannot be undone.", color = C.mut) },
            confirmButton = { TextButton(onClick = { st.resetAll(); confirm = false }) { Text("Reset", color = C.red) } },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel", color = C.mut) } },
        )
    }
}
