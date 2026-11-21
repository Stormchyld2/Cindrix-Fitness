package com.cindrix.fitness

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private class TrackerDef(val id: String, val title: String, val blurb: String, val fields: List<Pair<String, String>>)

private val TRACKERS = listOf(
    TrackerDef("weight", "Body weight", "Weigh in the same way each time.", listOf("weight" to "Weight (lb)")),
    TrackerDef("measure", "Measurements", "Waist, chest, hips, arm and thigh in inches.",
        listOf("waist" to "Waist (in)", "chest" to "Chest (in)", "hips" to "Hips (in)", "arm" to "Arm (in)", "thigh" to "Thigh (in)")),
    TrackerDef("recovery", "Recovery", "Sleep, energy and soreness.",
        listOf("sleep" to "Sleep (hours)", "energy" to "Energy (1–10)", "soreness" to "Soreness (1–10)")),
    TrackerDef("steps", "Walking", "Daily steps toward your target.", listOf("steps" to "Steps")),
    TrackerDef("boxing", "Boxing rounds", "Rounds logged from the timer or by hand.", listOf("rounds" to "Rounds today")),
    TrackerDef("slam", "Slam ball rounds", "Rounds logged from the timer or by hand.", listOf("rounds" to "Rounds today")),
)

@Composable
fun ProgressScreen(st: AppState, nav: Nav) {
    val weights = st.entriesFor("weight").mapNotNull { it.values["weight"] }
    Page {
        Header("Progress", "Week ${st.programWeek()} of 12")
        CCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Stat("${st.workouts.size}", "workouts")
                Stat("${st.setLogs.size}", "sets")
                Stat("${st.streakWeeks()}", "week streak")
                Stat("${st.bandUps}", "band-ups")
            }
        }
        CCard {
            Text("Body weight", color = C.txt, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            LineChart(weights.map { it.toFloat() }.takeLast(12), st.entriesFor("weight").takeLast(12).map { it.date.takeLast(5) })
        }
        NavCard("Trackers", "Weight, measurements, recovery, walking, rounds") { nav.go(Screen.Trackers) }
        NavCard("Band levels", "See and set the band for every move") { nav.go(Screen.BandLevels) }
        NavCard("Workout log", "${st.workouts.size} workouts completed") { nav.go(Screen.WorkoutHistory) }
        NavCard("Rewards", "${st.rank()} · level ${st.level()} · badges") { nav.go(Screen.Rewards) }
        NavCard("Nutrition", "Calories, protein, water and the Mediterranean guide") { nav.go(Screen.Nutrition) }
        NavCard("Joint check guide", "Green, yellow and red zones") { nav.go(Screen.Joint) }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Stat(v: String, label: String) {
    androidx.compose.foundation.layout.Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(v, color = C.acc, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = C.mut, fontSize = 11.sp)
    }
}

@Composable
fun NavCard(title: String, sub: String, onClick: () -> Unit) {
    CCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                Text(title, color = C.txt, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                Muted(sub, 12)
            }
            Text("›", color = C.mut, fontSize = 24.sp)
        }
    }
}

@Composable
fun TrackersScreen(st: AppState, nav: Nav) {
    Page {
        Header("Trackers", "Tap one to log or review", onBack = { nav.back() })
        TRACKERS.forEach { t ->
            NavCard(t.title, t.blurb) { nav.go(Screen.Tracker(t.id)) }
        }
    }
}

@Composable
fun TrackerScreen(st: AppState, nav: Nav, id: String) {
    val def = TRACKERS.firstOrNull { it.id == id } ?: TRACKERS.first()
    val today = st.todayEntry(def.id)
    val inputs = remember(def.id) {
        mutableStateMapOf<String, String>().also { m -> def.fields.forEach { (k, _) -> m[k] = today[k]?.let { fmt(it) } ?: "" } }
    }
    val history = st.entriesFor(def.id)
    val firstKey = def.fields.first().first
    Page {
        Header(def.title, def.blurb, onBack = { nav.back() })
        CCard {
            def.fields.forEach { (k, label) ->
                OutlinedTextField(
                    value = inputs[k] ?: "", onValueChange = { inputs[k] = it }, label = { Text(label) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                )
            }
            Row(Modifier.padding(top = 8.dp)) {
                PrimaryButton("Save today", Modifier.fillMaxWidth()) {
                    val vals = def.fields.mapNotNull { (k, _) -> inputs[k]?.toDoubleOrNull()?.let { k to it } }.toMap()
                    if (vals.isNotEmpty()) st.saveEntry(def.id, vals)
                }
            }
        }
        CCard {
            Text("Trend", color = C.txt, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            LineChart(
                history.mapNotNull { it.values[firstKey]?.toFloat() }.takeLast(14),
                history.filter { it.values[firstKey] != null }.takeLast(14).map { it.date.takeLast(5) },
            )
        }
        SectionTitle("History")
        if (history.isEmpty()) Muted("Nothing logged yet.")
        history.reversed().take(14).forEach { e ->
            CCard(pad = 10.dp) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(e.date, color = C.mut, fontSize = 12.sp)
                    Text(def.fields.mapNotNull { (k, l) -> e.values[k]?.let { "${l.substringBefore(" (")} ${fmt(it)}" } }.joinToString(" · "), color = C.txt, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun JointScreen(st: AppState, nav: Nav) {
    Page {
        Header("Joint check guide", "Use before and during every set", onBack = { nav.back() })
        CCard {
            Text("Green", color = C.green, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Muted("No pain. Muscle effort only. Train as planned.")
        }
        CCard {
            Text("Yellow", color = C.yellow, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Muted("Mild discomfort or something feels off. Reduce resistance or range, slow down, use the substitute movement, or drop a set. Keep going only once it settles.")
        }
        CCard {
            Text("Red", color = C.red, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Muted("Sharp pain, pinching, numbness or tingling. Stop that movement for the day.")
        }
        SectionTitle("Watch points")
        listOf(
            "Back" to "Keep ribs down and the trunk braced. Slams are straight only, no twisting or jumping. If the low back goes yellow, use the Pallof press and chest-height slam.",
            "Knees" to "Use a controlled range. If the knees go yellow, switch to the box squat or reduce the band.",
            "Ankles" to "Keep feet planted and stable on band and bag work; slow the tempo if an ankle feels off.",
            "Shoulders" to "Control overhead pressing and face pulls. If the shoulder goes yellow, lighten the band or use the single-arm option.",
            "Wrists" to "Hand wraps and gloves for every bag round, wrist straight when punching.",
        ).forEach { (a, t) ->
            CCard { Text(a, color = C.acc, fontSize = 14.sp, fontWeight = FontWeight.Bold); Muted(t, 13) }
        }
        Muted("This is educational guidance from your Hybrid guide. It does not replace medical care or physical therapy. See a clinician for pain that persists.", 12)
    }
}

@Composable
fun BandLevelsScreen(st: AppState, nav: Nav) {
    Page {
        Header("Band levels", "Tap a band to set it for that move", onBack = { nav.back() })
        Library.all.filter { it.startLb > 0 && it.timed == null }.forEach { ex ->
            val cur = st.bandFor(ex)
            CCard(pad = 10.dp) {
                Text(ex.name, color = C.txt, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BANDS.forEach { b -> Chip("$b", cur == b, Modifier.weight(1f)) { st.setBand(ex.id, b) } }
                }
            }
        }
    }
}

@Composable
fun WorkoutHistoryScreen(st: AppState, nav: Nav) {
    Page {
        Header("Workout log", "${st.workouts.size} workouts · ${st.setLogs.size} sets", onBack = { nav.back() })
        if (st.workouts.isEmpty()) Muted("No workouts yet. Finish one and it shows up here.")
        st.workouts.reversed().forEach { w ->
            CCard(pad = 10.dp) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${w.date} · ${w.day}", color = C.txt, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(if (w.flagged) "flagged" else "clean", color = if (w.flagged) C.yellow else C.green, fontSize = 12.sp)
                }
                Muted("${w.sets} sets logged", 12)
            }
        }
    }
}
