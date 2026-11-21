package com.cindrix.fitness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    private lateinit var state: AppState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Recipes.load(applicationContext)
        state = AppState(applicationContext)
        setContent {
            CindrixTheme {
                AppRoot(state)
            }
        }
    }
}
