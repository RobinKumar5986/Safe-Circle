package com.kgjr.safecircle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.navigation.compose.rememberNavController
import com.kgjr.safecircle.theme.SafeCircleTheme
import com.kgjr.safecircle.ui.navigationGraph.MainGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val destination: String = intent.getStringExtra("destination").toString()
        setContent {
            SafeCircleTheme {
                enableEdgeToEdge()
                Column {
                    val navController = rememberNavController()
                    MainGraph(navController = navController,destination = destination)
                }
            }
        }
    }
}
