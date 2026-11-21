package com.cindrix.fitness

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed interface Screen {
    data class WorkoutDay(val day: String) : Screen
    data class Active(val day: String) : Screen
    data class DayEditor(val day: String) : Screen
    data class Picker(val day: String, val replace: Int?) : Screen
    data object Rewards : Screen
    data object Trackers : Screen
    data class Tracker(val id: String) : Screen
    data object Joint : Screen
    data object Nutrition : Screen
    data object Mediterranean : Screen
    data object MealPlanner : Screen
    data object Recipes : Screen
    data class Recipe(val id: String) : Screen
    data object Shopping : Screen
    data object Settings : Screen
    data object TimerHub : Screen
    data class Timer(val exId: String) : Screen
    data object BandLevels : Screen
    data object WorkoutHistory : Screen
}

class Nav(private val stack: SnapshotStateList<Screen>, private val showEx: (String) -> Unit) {
    fun go(s: Screen) {
        stack.add(s)
    }

    fun back() {
        if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex)
    }

    fun openEx(id: String) = showEx(id)
}

@Composable
fun CindrixTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = C.acc, onPrimary = C.bg, background = C.bg, onBackground = C.txt,
            surface = C.surf, onSurface = C.txt, surfaceVariant = C.surf, onSurfaceVariant = C.mut,
            outline = C.line, secondaryContainer = C.line, onSecondaryContainer = C.txt,
        ),
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(st: AppState) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val stack = remember { mutableStateListOf<Screen>() }
    var sheet by remember { mutableStateOf<String?>(null) }
    val nav = remember { Nav(stack) { sheet = it } }

    BackHandler(enabled = stack.isNotEmpty()) { nav.back() }

    Scaffold(
        containerColor = C.bg,
        bottomBar = {
            if (stack.isEmpty()) {
                NavigationBar(containerColor = C.surf) {
                    val items = listOf(
                        "Today" to Icons.Filled.Home,
                        "Program" to Icons.Filled.DateRange,
                        "Workouts" to Icons.Filled.PlayArrow,
                        "Progress" to Icons.Filled.Star,
                        "Library" to Icons.Filled.Menu,
                    )
                    items.forEachIndexed { i, (label, icon) ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = C.acc, selectedTextColor = C.acc, indicatorColor = C.line,
                                unselectedIconColor = C.mut, unselectedTextColor = C.mut,
                            ),
                        )
                    }
                }
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize().background(C.bg)) {
            when (val s = stack.lastOrNull()) {
                null -> when (tab) {
                    0 -> TodayScreen(st, nav)
                    1 -> ProgramScreen(st, nav)
                    2 -> WorkoutsScreen(st, nav)
                    3 -> ProgressScreen(st, nav)
                    else -> LibraryScreen(nav)
                }
                is Screen.WorkoutDay -> WorkoutDayScreen(st, nav, s.day)
                is Screen.Active -> ActiveWorkoutScreen(st, nav, s.day)
                is Screen.DayEditor -> DayEditorScreen(st, nav, s.day)
                is Screen.Picker -> PickerScreen(st, nav, s.day, s.replace)
                Screen.Rewards -> RewardsScreen(st, nav)
                Screen.Trackers -> TrackersScreen(st, nav)
                is Screen.Tracker -> TrackerScreen(st, nav, s.id)
                Screen.Joint -> JointScreen(st, nav)
                Screen.Nutrition -> NutritionScreen(st, nav)
                Screen.Mediterranean -> MediterraneanScreen(nav)
                Screen.MealPlanner -> MealPlannerScreen(st, nav)
                Screen.Recipes -> RecipesScreen(st, nav)
                is Screen.Recipe -> RecipeScreen(st, nav, s.id)
                Screen.Shopping -> ShoppingScreen(st, nav)
                Screen.Settings -> SettingsScreen(st, nav)
                Screen.TimerHub -> TimerHubScreen(nav)
                is Screen.Timer -> TimerScreen(st, nav, s.exId)
                Screen.BandLevels -> BandLevelsScreen(st, nav)
                Screen.WorkoutHistory -> WorkoutHistoryScreen(st, nav)
            }
        }
    }

    val id = sheet
    if (id != null) {
        val ex = Library.get(id)
        if (ex != null) {
            ModalBottomSheet(onDismissRequest = { sheet = null }, containerColor = C.surf) {
                ExerciseSheetContent(ex, nav)
            }
        }
    }
}

@Composable
fun ExerciseSheetContent(ex: Exercise, nav: Nav) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(ex.group.uppercase(), color = C.acc, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(ex.name, color = C.txt, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        ExercisePhoto(ex.id, Modifier.fillMaxWidth().aspectRatio(1.5f))
        if (!ex.inGuide) Pill("Not in your guide", C.yellow)
        Text("Setup: ${ex.setup}", color = C.txt, fontSize = 14.sp)
        if (ex.timed == null) {
            Text("Starting resistance: ${ex.startNote}", color = C.txt, fontSize = 14.sp)
            Text("Prescription: ${ex.rx}", color = C.txt, fontSize = 14.sp)
        } else {
            Text(
                "Plan: ${ex.timed.rounds} round(s), ${ex.timed.workSec / 60}:${"%02d".format(ex.timed.workSec % 60)} work" +
                    if (ex.timed.restSec > 0) ", ${ex.timed.restSec} s rest" else "",
                color = C.txt, fontSize = 14.sp,
            )
        }
        SectionTitle("How to do it")
        ex.cues.forEachIndexed { i, c ->
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${i + 1}", color = C.acc, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Text(c, color = C.txt, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
        if (ex.timed != null && ex.timed.focus.size > 1) {
            SectionTitle("Round focus")
            ex.timed.focus.forEach { Text(it, color = C.mut, fontSize = 13.sp) }
        }
        if (ex.sub.isNotEmpty()) {
            CCard(onClick = ex.subId?.let { id -> { nav.openEx(id) } }) {
                Text("Substitute / modification", color = C.mut, fontSize = 12.sp)
                Text(ex.sub, color = C.txt, fontSize = 14.sp)
                if (ex.subId != null) Text("Tap to see how", color = C.acc, fontSize = 12.sp)
            }
        }
        Text(
            "Yellow joint zone: reduce resistance or range, slow down, use the substitute, or drop a set. " +
                "Red: stop the movement.",
            color = C.mut, fontSize = 12.sp, lineHeight = 17.sp,
        )
    }
}
