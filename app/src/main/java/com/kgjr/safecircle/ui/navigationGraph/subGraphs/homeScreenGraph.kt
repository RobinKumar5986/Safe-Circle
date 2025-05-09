package com.kgjr.safecircle.ui.navigationGraph.subGraphs

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.ui.navigationGraph.NavigationDestinations

fun NavGraphBuilder.homeScreenGraph(navController: NavController){
    navigation(
        route = NavigationDestinations.homeScreenMain, //@Note: rout for the main graph
        startDestination = NavigationDestinations.homeScreen //Starting destination
    ) {
        composable(NavigationDestinations.homeScreen) {
            Button(onClick = {
                MainApplication.setLoginState(false)
            }) {
                Text("Click to log out")
            }
        }
    }
}