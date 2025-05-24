package com.kgjr.safecircle.ui.navigationGraph.subGraphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kgjr.safecircle.ui.layouts.AllPermissionScreen
import com.kgjr.safecircle.ui.navigationGraph.NavigationDestinations


fun NavGraphBuilder.allPermissionGraph(navController: NavController) {
    navigation(
        route = NavigationDestinations.allPermissionMain, //@Note: rout for the main graph
        startDestination = NavigationDestinations.allPermission //Starting destination
    ) {
        composable(NavigationDestinations.allPermission) {
            AllPermissionScreen(){
                navController.navigate(NavigationDestinations.homeScreenMain) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            }
        }
    }
}