package com.cindrix.fitness

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private fun mmss(sec: Int): String = "${sec / 60}:${"%02d".format(sec % 60)}"

@Composable
fun RoundTimer(t: Timed, onFinished: (Int) -> Unit) {
    var running by remember { mutableStateOf(false) }
    var round by remember { mutableIntStateOf(1) }
    var rest by remember { mutableStateOf(false) }
    var left by remember { mutableIntStateOf(t.workSec) }
    var done by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        while (running) {
            delay(1000)
            left -= 1
            if (left <= 0) {
                if (!rest) {
                    if (round >= t.rounds) {
                        running = false
                        done = true
                        onFinished(t.rounds)
                    } else if (t.restSec > 0) {
                        rest = true
                        left = t.restSec
                    } else {
                        round += 1
                        left = t.workSec
                    }
                } else {
                    rest = false
                    round += 1
                    left = t.workSec
                }
            }
        }
    }

    CCard {
        val focus = t.focus.getOrNull(round - 1) ?: t.focus.firstOrNull() ?: ""
        Text(
            if (done) "All rounds complete" else if (rest) "REST · next is round ${round + 1}" else "Round $round of ${t.rounds}",
            color = if (rest) C.yellow else C.acc, fontSize = 13.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
        )
        Text(
            mmss(left.coerceAtLeast(0)), color = C.txt, fontSize = 56.sp, fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
        )
        if (focus.isNotEmpty() && !rest && !done) {
            Text(focus, color = C.mut, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        }
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(if (running) "Pause" else if (done) "Again" else "Start", Modifier.weight(1f)) {
                if (done) {
                    done = false; round = 1; rest = false; left = t.workSec; running = true
                } else {
                    running = !running
                }
            }
            OutlineButton("Reset", Modifier.weight(1f)) {
                running = false; done = false; round = 1; rest = false; left = t.workSec
            }
        }
    }
}

@Composable
fun TimerHubScreen(nav: Nav) {
    Page {
        Header("Round timer", "Pick a block", onBack = { nav.back() })
        listOf("heavy_bag", "boxing_machine", "slam_ball").forEach { id ->
            val ex = Library.get(id) ?: return@forEach
            val tm = ex.timed ?: return@forEach
            CCard(onClick = { nav.go(Screen.Timer(id)) }) {
                Text(ex.name, color = C.txt, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                Muted("${tm.rounds} rounds · ${mmss(tm.workSec)} work · ${tm.restSec} s rest")
            }
        }
    }
}

@Composable
fun TimerScreen(st: AppState, nav: Nav, exId: String) {
    val ex = Library.get(exId)
    val tm = ex?.timed
    Page {
        Header(ex?.name ?: "Timer", "Wear hand wraps and gloves for bag work", onBack = { nav.back() })
        if (tm != null) {
            RoundTimer(tm) { r -> if (exId != "slam_ball") st.addRounds("boxing", r) else st.addRounds("slam", r) }
            tm.focus.forEach { Muted(it) }
        }
    }
}

@Composable
fun ActiveWorkoutScreen(st: AppState, nav: Nav, day: String) {
    val items = st.plan[day].orEmpty().filter { Library.get(it.id) != null }
    var safe by remember { mutableStateOf(false) }
    var idx by remember { mutableIntStateOf(0) }
    var setsDone by remember { mutableIntStateOf(0) }
    var flagged by remember { mutableStateOf(false) }

    Page {
        Header(DAY_TITLES.getValue(day).substringBefore(" ·"), if (safe && idx < items.size) "Move ${idx + 1} of ${items.size}" else DAY_SHORT[day], onBack = { nav.back() })
        if (!safe) {
            CCard {
                Text("Safety check", color = C.txt, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                Muted("Check your joints before you start. Green: train as planned. Yellow: reduce resistance or range, slow down, use the substitute or drop a set. Red: stop that movement.")
                Muted("Check the band for cracks or wear and the anchor for security.")
            }
            PrimaryButton("All clear, start") { safe = true }
        } else if (idx >= items.size) {
            CCard {
                Text("Workout complete", color = C.green, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Muted("$setsDone sets logged. Finishing adds 100 XP.")
            }
            PrimaryButton("Finish workout") {
                st.finishWorkout(day, setsDone, flagged)
                nav.back()
            }
        } else {
            val p = items[idx]
            val ex = Library.getValue(p.id)
            key(idx) {
                if (ex.timed != null) {
                    TimedBlock(nav, ex, onDone = { idx += 1 })
                } else {
                    StrengthBlock(st, nav, ex, p, onLogged = { setsDone += 1 }, onFlag = { flagged = true }, onNext = { idx += 1 })
                }
            }
            OutlineButton("Skip this move", Modifier.fillMaxWidth()) { idx += 1 }
        }
    }
}

@Composable
private fun TimedBlock(nav: Nav, ex: Exercise, onDone: () -> Unit) {
    val tm = ex.timed ?: return
    var finished by remember { mutableStateOf(false) }
    CCard {
        ExercisePhoto(ex.id, Modifier.fillMaxWidth().aspectRatio(2.2f), compact = true)
        Text(ex.name, color = C.txt, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 8.dp))
        ex.cues.take(4).forEach { Muted("• $it", 12) }
        Box(Modifier.padding(top = 4.dp)) { OutlineButton("Full how-to") { nav.openEx(ex.id) } }
    }
    RoundTimer(tm) { finished = true }
    PrimaryButton(if (finished) "Next move" else "Done, next move") { onDone() }
}

@Composable
private fun StrengthBlock(st: AppState, nav: Nav, ex: Exercise, p: PlanItem, onLogged: () -> Unit, onFlag: () -> Unit, onNext: () -> Unit) {
    var setNo by remember { mutableIntStateOf(1) }
    var reps by remember { mutableIntStateOf(p.hi) }
    var rir by remember { mutableIntStateOf(2) }
    var joint by remember { mutableIntStateOf(0) }
    var adjusted by remember { mutableStateOf(false) }
    var restLeft by remember { mutableIntStateOf(0) }
    var session by remember { mutableStateOf(listOf<SetLog>()) }
    var bandPrompt by remember { mutableStateOf(false) }
    val usesBand = ex.startLb > 0
    val band = st.bandFor(ex)
    val last = st.setLogs.lastOrNull { it.exId == ex.id }

    LaunchedEffect(restLeft) {
        if (restLeft > 0) {
            delay(1000)
            restLeft -= 1
        }
    }

    CCard {
        ExercisePhoto(ex.id, Modifier.fillMaxWidth().aspectRatio(2.2f), compact = true)
        Text(ex.name, color = C.txt, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 8.dp))
        Muted(ex.setup, 12)
        if (last != null) Muted("Last time: ${if (last.lb > 0) "${last.lb} lb × " else ""}${last.reps} reps", 12)
        Muted(ex.cues.firstOrNull() ?: "", 12)
        Box(Modifier.padding(top = 4.dp)) { OutlineButton("Full how-to") { nav.openEx(ex.id) } }
    }

    if (bandPrompt) {
        CCard {
            Text("Ready for a heavier band?", color = C.txt, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Muted("You hit the top of the rep range on every set with no joint flags. Move up one band (+50 XP).")
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryButton("Band up", Modifier.weight(1f)) { st.bandUp(ex); onNext() }
                OutlineButton("Stay here", Modifier.weight(1f)) { onNext() }
            }
        }
        return
    }

    if (setNo > p.sets) {
        CCard {
            Text("${ex.name} done", color = C.green, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
        PrimaryButton("Next move") { onNext() }
        return
    }

    CCard {
        Text("Set $setNo of ${p.sets}", color = C.acc, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        if (usesBand) {
            Muted("Band", 12)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BANDS.forEach { b -> Chip("$b", band == b, Modifier.weight(1f)) { st.setBand(ex.id, b) } }
            }
        }
        StepRow("Reps${if (ex.perSide) " each side" else ""}", reps, 1, 40) { reps = it }
        Muted("Reps in reserve (0 = nothing left)", 12)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (0..4).forEach { r -> Chip("$r", rir == r, Modifier.weight(1f)) { rir = r } }
        }
        Muted("Joint check", 12)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Chip("Green", joint == 0, Modifier.weight(1f), C.green) { joint = 0; adjusted = false }
            Chip("Yellow", joint == 1, Modifier.weight(1f), C.yellow) { joint = 1 }
            Chip("Red", joint == 2, Modifier.weight(1f), C.red) { joint = 2; adjusted = false }
        }
        if (joint == 1) {
            Muted("Reduce resistance or range, slow down, or use the substitute: ${ex.sub}", 12)
            Chip(if (adjusted) "✓ I adjusted" else "Tap when you have adjusted", adjusted, Modifier.fillMaxWidth(), C.yellow) { adjusted = !adjusted }
        }
        if (joint == 2) {
            Text("Stop this movement. Skip it today.", color = C.red, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
        }
        Box(Modifier.padding(top = 8.dp)) {
            PrimaryButton(
                if (joint == 2) "Stop and skip this move" else "Log set",
                Modifier.fillMaxWidth(),
                enabled = joint != 1 || adjusted,
            ) {
                val sl = SetLog(today(), ex.id, setNo, if (usesBand) band else 0, reps, rir, joint, adjusted)
                st.logSet(sl)
                onLogged()
                if (joint != 0) onFlag()
                if (joint == 2) {
                    onNext()
                } else {
                    session = session + sl
                    if (setNo >= p.sets) {
                        val eligible = usesBand && band < BANDS.last() &&
                            session.all { it.reps >= p.hi && it.joint == 0 && it.lb == band }
                        setNo += 1
                        if (eligible) bandPrompt = true
                    } else {
                        setNo += 1
                        restLeft = ex.restSec
                    }
                    joint = 0; adjusted = false
                }
            }
        }
    }
    if (restLeft > 0) {
        CCard {
            Text("Rest ${mmss(restLeft)}", color = C.acc, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            OutlineButton("Skip rest", Modifier.fillMaxWidth()) { restLeft = 0 }
        }
    }
}
