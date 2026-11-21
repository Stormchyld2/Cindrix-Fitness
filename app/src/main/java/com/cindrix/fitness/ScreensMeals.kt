package com.cindrix.fitness

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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

data class Meal(val name: String, val cal: Int, val pro: Int)

val SLOTS = listOf("Breakfast", "Lunch", "Dinner", "Snack")
val WEEK = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

// Calories and protein are rough estimates for a typical portion; they vary with portion size.
object MealLib {
    val options: Map<String, List<Meal>> = mapOf(
        "Breakfast" to listOf(
            Meal("Greek yogurt, berries, oats and walnuts", 400, 30),
            Meal("Veggie omelet with feta, whole-grain toast and olive oil", 450, 30),
            Meal("Cottage cheese bowl with fruit and nuts", 380, 32),
            Meal("Overnight oats with chia, fruit and almond butter", 420, 20),
            Meal("Scrambled eggs, spinach and tomatoes on whole-grain toast", 430, 28),
        ),
        "Lunch" to listOf(
            Meal("Chicken, chickpea and vegetable salad with olive oil and lemon", 550, 48),
            Meal("Tuna and white bean salad", 500, 45),
            Meal("Lentil soup with whole-grain bread and a side salad", 520, 30),
            Meal("Chicken gyro bowl with tzatziki, cucumber and brown rice", 620, 50),
            Meal("Hummus, grilled chicken and vegetable whole-grain wrap", 560, 45),
        ),
        "Dinner" to listOf(
            Meal("Baked salmon, roasted vegetables and brown rice", 650, 45),
            Meal("Shrimp, tomato and garlic with whole-grain pasta", 600, 42),
            Meal("Grilled chicken with quinoa tabbouleh", 620, 50),
            Meal("Turkey meatballs in tomato sauce with zucchini", 560, 48),
            Meal("Baked cod with olives, tomatoes and potatoes", 520, 42),
        ),
        "Snack" to listOf(
            Meal("Hummus and raw vegetables", 200, 8),
            Meal("Greek yogurt with honey and nuts", 250, 20),
            Meal("Cottage cheese and fruit", 200, 22),
            Meal("Almonds and an apple", 250, 7),
            Meal("Hard-boiled eggs and olives", 220, 14),
        ),
    )

    fun autoFill(): Map<String, Meal> {
        val m = mutableMapOf<String, Meal>()
        WEEK.forEachIndexed { d, day ->
            SLOTS.forEach { slot ->
                val l = options.getValue(slot)
                m["$day|$slot"] = l[(d + SLOTS.indexOf(slot)) % l.size]
            }
        }
        return m
    }
}

@Composable
fun MealPlannerScreen(st: AppState, nav: Nav) {
    var day by remember { mutableStateOf(WEEK[java.time.LocalDate.now().dayOfWeek.value - 1]) }
    var picking by remember { mutableStateOf<String?>(null) }
    val meals = SLOTS.map { it to st.mealPlan["$day|$it"] }
    val totalCal = meals.sumOf { it.second?.cal ?: 0 }
    val totalPro = meals.sumOf { it.second?.pro ?: 0 }
    Page {
        Header("Meal planner", "Plan the week, then log meals as you eat", onBack = { nav.back() })
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            WEEK.forEach { d -> Chip(d, day == d, Modifier.weight(1f)) { day = d } }
        }
        CCard {
            Text("$day totals", color = C.txt, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Muted("$totalCal / ${st.goals.calories} cal · $totalPro / ${st.goals.protein} g protein (estimates)")
            Bar(totalPro.toFloat() / st.goals.protein)
        }
        meals.forEach { (slot, meal) ->
            CCard(onClick = { picking = slot }) {
                Text(slot.uppercase(), color = C.acc, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                if (meal == null) {
                    Muted("Tap to choose a meal")
                } else {
                    Text(meal.name, color = C.txt, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Muted("≈ ${meal.cal} cal · ${meal.pro} g protein", 12)
                }
            }
            if (meal != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlineButton("Log to today", Modifier.weight(1f)) { st.addNutrition(meal.cal.toDouble(), meal.pro.toDouble(), 0.0) }
                    OutlineButton("Clear", Modifier.weight(1f), C.mut) { st.setMeal("$day|$slot", null) }
                }
            }
        }
        PrimaryButton("Auto-fill the whole week", Modifier.fillMaxWidth()) { st.autoFillMeals() }
        OutlineButton("Clear the week", Modifier.fillMaxWidth(), C.mut) { st.clearMeals() }
        Muted("Mediterranean-style ideas with rough numbers. Adjust portions to hit your targets. General education, not dietitian advice.", 12)
    }
    val slot = picking
    if (slot != null) {
        var custom by remember(slot) { mutableStateOf(false) }
        var name by remember(slot) { mutableStateOf("") }
        var cal by remember(slot) { mutableStateOf("") }
        var pro by remember(slot) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { picking = null },
            containerColor = C.surf,
            title = { Text("$day · $slot", color = C.txt, fontSize = 18.sp) },
            text = {
                Column(Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!custom) {
                        MealLib.options.getValue(slot).forEach { m ->
                            CCard(pad = 10.dp, onClick = { st.setMeal("$day|$slot", m); picking = null }) {
                                Text(m.name, color = C.txt, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Muted("≈ ${m.cal} cal · ${m.pro} g protein", 12)
                            }
                        }
                        OutlineButton("Enter my own meal", Modifier.fillMaxWidth()) { custom = true }
                    } else {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Meal") }, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = cal, onValueChange = { cal = it }, label = { Text("Cal", fontSize = 11.sp) }, singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = pro, onValueChange = { pro = it }, label = { Text("Protein g", fontSize = 11.sp) }, singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f),
                            )
                        }
                        Row(Modifier.padding(top = 6.dp)) {
                            PrimaryButton("Save meal", Modifier.fillMaxWidth(), enabled = name.isNotBlank()) {
                                st.setMeal("$day|$slot", Meal(name.trim(), cal.toIntOrNull() ?: 0, pro.toIntOrNull() ?: 0))
                                picking = null
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { picking = null }) { Text("Cancel", color = C.mut) } },
        )
    }
}
