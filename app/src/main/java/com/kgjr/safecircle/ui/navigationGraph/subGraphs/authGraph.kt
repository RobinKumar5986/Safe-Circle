package com.kgjr.safecircle.ui.navigationGraph.subGraphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kgjr.safecircle.ui.layouts.SignUpView
import com.kgjr.safecircle.ui.navigationGraph.NavigationDestinations

fun NavGraphBuilder.authGraph(navController: NavController) {
    navigation(
        route = NavigationDestinations.loginScreenMain, //@Note: rout for the main graph
        startDestination = NavigationDestinations.loginScreen //Starting destination
    ) {
        composable(NavigationDestinations.loginScreen) {
            SignUpView()
        }
    }
}