package com.cindrix.fitness

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

data class PlanItem(val id: String, val sets: Int, val lo: Int, val hi: Int)
data class SetLog(
    val date: String, val exId: String, val setNo: Int, val lb: Int, val reps: Int,
    val rir: Int, val joint: Int, val adjusted: Boolean,
)
data class WorkoutLog(val date: String, val day: String, val sets: Int, val flagged: Boolean)
data class Entry(val date: String, val tracker: String, val values: Map<String, Double>)
data class Goals(val calories: Int = 2400, val protein: Int = 190, val water: Int = 100, val steps: Int = 6000)
data class Badge(val name: String, val how: String, val earned: Boolean)

val DAY_KEYS = listOf("Mon", "Wed", "Fri")
val DAY_TITLES = mapOf(
    "Mon" to "Monday · Chest + Back + Arms + Heavy Bag",
    "Wed" to "Wednesday · Legs + Glutes + Hamstrings + Core + Boxing Machine",
    "Fri" to "Friday · Full Body + Shoulders + Chest + Triceps + Heavy Bag",
)
val DAY_SHORT = mapOf("Mon" to "Chest + Back + Arms", "Wed" to "Legs + Glutes + Core", "Fri" to "Full Body + Shoulders")
val BANDS = listOf(15, 25, 35, 45, 55)

fun today(): String = LocalDate.now().toString()
fun weekStart(d: LocalDate): LocalDate = d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
fun weekKey(date: String): String = weekStart(LocalDate.parse(date)).toString()

fun item(id: String): PlanItem {
    val e = Library.byId.getValue(id)
    return PlanItem(id, e.sets, e.repsLo, e.repsHi)
}

object Presets {
    private fun list(vararg ids: String) = ids.map { item(it) }

    val guide: Map<String, List<PlanItem>> = mapOf(
        "Mon" to list("warmup", "chest_press", "seated_row", "lat_pulldown", "triceps_pressdown", "bar_curl",
            "dead_bug", "slam_ball", "heavy_bag", "cooldown"),
        "Wed" to list("warmup", "band_bar_squat", "pull_through", "hamstring_curl", "glute_kickback", "pallof_press",
            "slam_ball", "boxing_machine", "cooldown"),
        "Fri" to list("warmup", "band_bar_squat", "overhead_press", "face_pull", "chest_press", "triceps_pressdown",
            "dead_bug", "slam_ball", "heavy_bag", "cooldown"),
    )

    // Friday uses the guide's own substitute movements so no strength move repeats across the three days.
    val varied: Map<String, List<PlanItem>> = mapOf(
        "Mon" to guide.getValue("Mon"),
        "Wed" to guide.getValue("Wed"),
        "Fri" to list("warmup", "box_squat", "overhead_press", "face_pull", "single_arm_chest_press",
            "single_arm_pressdown", "pallof_press", "slam_ball", "heavy_bag", "cooldown"),
    )
}

class AppState(context: Context) {
    private val prefs = context.getSharedPreferences("cindrix_fitness", Context.MODE_PRIVATE)

    var plan by mutableStateOf<Map<String, List<PlanItem>>>(Presets.varied)
        private set
    var setLogs by mutableStateOf<List<SetLog>>(emptyList())
        private set
    var workouts by mutableStateOf<List<WorkoutLog>>(emptyList())
        private set
    var entries by mutableStateOf<List<Entry>>(emptyList())
        private set
    var bands by mutableStateOf<Map<String, Int>>(emptyMap())
        private set
    var xp by mutableIntStateOf(0)
        private set
    var bandUps by mutableIntStateOf(0)
        private set
    var questsPaid by mutableStateOf<Set<String>>(emptySet())
        private set
    var shopChecked by mutableStateOf<Set<String>>(emptySet())
        private set
    var goals by mutableStateOf(Goals())
        private set
    var startDate by mutableStateOf(today())
        private set

    init {
        load()
    }

    // ---------- plan ----------
    fun setPlan(day: String, items: List<PlanItem>) {
        plan = plan + (day to items)
        save()
    }

    fun usePreset(varied: Boolean) {
        plan = if (varied) Presets.varied else Presets.guide
        save()
    }

    // ---------- workout logging ----------
    fun bandFor(ex: Exercise): Int = bands[ex.id] ?: ex.startLb

    fun logSet(sl: SetLog) {
        setLogs = setLogs + sl
        val counted = sl.joint == 0 || (sl.joint == 1 && sl.adjusted)
        if (counted) xp += 10
        save()
    }

    fun setBand(exId: String, lb: Int) {
        bands = bands + (exId to lb)
        save()
    }

    fun bandUp(ex: Exercise) {
        val cur = bandFor(ex)
        val next = BANDS.firstOrNull { it > cur } ?: cur
        bands = bands + (ex.id to next)
        bandUps += 1
        xp += 50
        save()
    }

    fun finishWorkout(day: String, sets: Int, flagged: Boolean) {
        workouts = workouts + WorkoutLog(today(), day, sets, flagged)
        xp += 100
        checkQuests()
        save()
    }

    fun addRounds(tracker: String, rounds: Int) {
        val cur = entries.firstOrNull { it.date == today() && it.tracker == tracker }?.values ?: emptyMap()
        val total = (cur["rounds"] ?: 0.0) + rounds
        saveEntry(tracker, cur + ("rounds" to total))
    }

    // ---------- trackers ----------
    fun entriesFor(tracker: String): List<Entry> =
        entries.filter { it.tracker == tracker }.sortedBy { it.date }

    fun todayEntry(tracker: String): Map<String, Double> =
        entries.firstOrNull { it.date == today() && it.tracker == tracker }?.values ?: emptyMap()

    fun saveEntry(tracker: String, values: Map<String, Double>, date: String = today()) {
        entries = entries.filterNot { it.date == date && it.tracker == tracker } + Entry(date, tracker, values)
        checkQuests()
        save()
    }

    fun addNutrition(cal: Double, protein: Double, water: Double) {
        val cur = todayEntry("nutrition")
        saveEntry(
            "nutrition",
            mapOf(
                "calories" to (cur["calories"] ?: 0.0) + cal,
                "protein" to (cur["protein"] ?: 0.0) + protein,
                "water" to (cur["water"] ?: 0.0) + water,
            ),
        )
    }

    fun toggleShop(item: String) {
        shopChecked = if (item in shopChecked) shopChecked - item else shopChecked + item
        save()
    }

    fun updateGoals(g: Goals) {
        goals = g
        save()
    }

    fun resetStart() {
        startDate = today()
        save()
    }

    fun resetAll() {
        plan = Presets.varied
        setLogs = emptyList(); workouts = emptyList(); entries = emptyList(); bands = emptyMap()
        xp = 0; bandUps = 0; questsPaid = emptySet(); shopChecked = emptySet(); goals = Goals()
        startDate = today()
        save()
    }

    // ---------- rewards ----------
    fun level(): Int = xp / 1000 + 1
    fun rank(): String = when (level()) {
        1, 2 -> "Recruit"
        3, 4 -> "Operator"
        5, 6 -> "Specialist"
        7, 8, 9 -> "Veteran"
        else -> "Chief"
    }
    fun nextRank(): String = when (level()) {
        1, 2 -> "Operator at level 3"
        3, 4 -> "Specialist at level 5"
        5, 6 -> "Veteran at level 7"
        7, 8, 9 -> "Chief at level 10"
        else -> "Top rank"
    }
    fun xpIntoLevel(): Int = xp % 1000

    fun programWeek(): Int {
        val days = ChronoUnit.DAYS.between(LocalDate.parse(startDate), LocalDate.now()).toInt()
        return (days / 7 + 1).coerceAtLeast(1)
    }

    fun workoutsInWeek(wk: String): Int = workouts.count { weekKey(it.date) == wk }
    fun workoutsThisWeek(): Int = workoutsInWeek(weekStart(LocalDate.now()).toString())

    private fun daysWith(tracker: String, field: String, goal: Double, wk: String): Int =
        entries.count { it.tracker == tracker && weekKey(it.date) == wk && (it.values[field] ?: 0.0) >= goal }

    fun proteinDaysThisWeek(): Int =
        daysWith("nutrition", "protein", goals.protein.toDouble(), weekStart(LocalDate.now()).toString())

    fun stepDaysThisWeek(): Int =
        daysWith("steps", "steps", goals.steps.toDouble(), weekStart(LocalDate.now()).toString())

    fun questsDone(): Boolean = workoutsThisWeek() >= 3 && proteinDaysThisWeek() >= 5 && stepDaysThisWeek() >= 4

    private fun checkQuests() {
        val wk = weekStart(LocalDate.now()).toString()
        if (wk !in questsPaid && questsDone()) {
            questsPaid = questsPaid + wk
            xp += 150
        }
    }

    fun streakWeeks(): Int {
        var wk = weekStart(LocalDate.now())
        var n = 0
        if (workoutsInWeek(wk.toString()) >= 3) n++
        wk = wk.minusWeeks(1)
        while (workoutsInWeek(wk.toString()) >= 3) {
            n++
            wk = wk.minusWeeks(1)
        }
        return n
    }

    fun badges(): List<Badge> {
        val cleanWeek = workouts.groupBy { weekKey(it.date) }.any { (_, l) -> l.size >= 3 && l.none { it.flagged } }
        val rounds = entries.filter { it.tracker == "boxing" }.sumOf { it.values["rounds"] ?: 0.0 }
        val week = programWeek()
        val done = workouts.size
        return listOf(
            Badge("First workout", "Finish one workout", done >= 1),
            Badge("First band-up", "Move up a band", bandUps >= 1),
            Badge("10 sessions", "Finish 10 workouts", done >= 10),
            Badge("4-week streak", "3 workouts a week, 4 weeks running", streakWeeks() >= 4),
            Badge("Clean week", "3 workouts, no yellow or red sets", cleanWeek),
            Badge("5 boxing rounds", "Log 5 boxing rounds", rounds >= 5),
            Badge("Protein week", "Hit protein 5 days in a week", questsPaid.isNotEmpty() || proteinDaysThisWeek() >= 5),
            Badge("8-week check", "Reach week 8 with 18+ workouts", week >= 8 && done >= 18),
            Badge("12-week finish", "Reach week 12 with 30+ workouts", week >= 12 && done >= 30),
        )
    }

    // ---------- persistence ----------
    private fun save() {
        val o = JSONObject()
        o.put("start", startDate)
        o.put("xp", xp)
        o.put("bandUps", bandUps)
        o.put("goals", JSONObject().put("c", goals.calories).put("p", goals.protein).put("w", goals.water).put("s", goals.steps))
        o.put("questsPaid", JSONArray(questsPaid.toList()))
        o.put("shop", JSONArray(shopChecked.toList()))
        o.put("bands", JSONObject(bands.mapValues { it.value }))
        val p = JSONObject()
        for ((d, l) in plan) {
            p.put(d, JSONArray(l.map { JSONObject().put("id", it.id).put("s", it.sets).put("lo", it.lo).put("hi", it.hi) }))
        }
        o.put("plan", p)
        o.put("sets", JSONArray(setLogs.map {
            JSONObject().put("d", it.date).put("e", it.exId).put("n", it.setNo).put("lb", it.lb)
                .put("r", it.reps).put("rir", it.rir).put("j", it.joint).put("a", it.adjusted)
        }))
        o.put("workouts", JSONArray(workouts.map {
            JSONObject().put("d", it.date).put("day", it.day).put("s", it.sets).put("f", it.flagged)
        }))
        o.put("entries", JSONArray(entries.map {
            JSONObject().put("d", it.date).put("t", it.tracker).put("v", JSONObject(it.values.mapValues { v -> v.value }))
        }))
        prefs.edit().putString("state", o.toString()).apply()
    }

    private fun load() {
        val raw = prefs.getString("state", null) ?: return
        try {
            val o = JSONObject(raw)
            startDate = o.optString("start", today())
            xp = o.optInt("xp", 0)
            bandUps = o.optInt("bandUps", 0)
            o.optJSONObject("goals")?.let {
                goals = Goals(it.optInt("c", 2400), it.optInt("p", 190), it.optInt("w", 100), it.optInt("s", 6000))
            }
            questsPaid = strings(o.optJSONArray("questsPaid"))
            shopChecked = strings(o.optJSONArray("shop"))
            o.optJSONObject("bands")?.let { b -> bands = b.keys().asSequence().associateWith { k -> b.getInt(k) } }
            o.optJSONObject("plan")?.let { p ->
                val m = mutableMapOf<String, List<PlanItem>>()
                for (d in p.keys()) {
                    val a = p.getJSONArray(d)
                    val l = mutableListOf<PlanItem>()
                    for (i in 0 until a.length()) {
                        val x = a.getJSONObject(i)
                        if (Library.byId.containsKey(x.getString("id"))) {
                            l.add(PlanItem(x.getString("id"), x.getInt("s"), x.getInt("lo"), x.getInt("hi")))
                        }
                    }
                    m[d] = l
                }
                if (m.isNotEmpty()) plan = m
            }
            val sa = o.optJSONArray("sets")
            if (sa != null) {
                setLogs = (0 until sa.length()).map {
                    val x = sa.getJSONObject(it)
                    SetLog(x.getString("d"), x.getString("e"), x.getInt("n"), x.getInt("lb"), x.getInt("r"),
                        x.getInt("rir"), x.getInt("j"), x.getBoolean("a"))
                }
            }
            val wa = o.optJSONArray("workouts")
            if (wa != null) {
                workouts = (0 until wa.length()).map {
                    val x = wa.getJSONObject(it)
                    WorkoutLog(x.getString("d"), x.getString("day"), x.getInt("s"), x.getBoolean("f"))
                }
            }
            val ea = o.optJSONArray("entries")
            if (ea != null) {
                entries = (0 until ea.length()).map {
                    val x = ea.getJSONObject(it)
                    val v = x.getJSONObject("v")
                    Entry(x.getString("d"), x.getString("t"), v.keys().asSequence().associateWith { k -> v.getDouble(k) })
                }
            }
        } catch (e: Exception) {
            // Corrupt data: keep defaults rather than crash.
        }
    }

    private fun strings(a: JSONArray?): Set<String> =
        if (a == null) emptySet() else (0 until a.length()).map { a.getString(it) }.toSet()
}
