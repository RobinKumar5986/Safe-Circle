package com.kgjr.safecircle.ui.navigationGraph

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.kgjr.safecircle.ui.navigationGraph.subGraphs.homeScreenGraph
import com.kgjr.safecircle.ui.navigationGraph.subGraphs.authGraph

@Composable
fun MainGraph(navController: NavHostController, modifier: Modifier = Modifier, destination: String){
    NavHost(navController = navController , startDestination = destination){
        authGraph(navController = navController)
        homeScreenGraph(navController = navController)
    }
}