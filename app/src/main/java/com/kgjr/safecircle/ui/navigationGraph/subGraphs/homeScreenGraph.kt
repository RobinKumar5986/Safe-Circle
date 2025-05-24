package com.kgjr.safecircle.ui.navigationGraph.subGraphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.ui.layouts.GroupScreen
import com.kgjr.safecircle.ui.navigationGraph.NavigationDestinations

fun NavGraphBuilder.homeScreenGraph(navController: NavController){
    navigation(
        route = NavigationDestinations.homeScreenMain, //@Note: rout for the main graph
        startDestination = NavigationDestinations.homeScreen //Starting destination
    ) {
        composable(NavigationDestinations.homeScreen) {
            GroupScreen(){ id ->
               when(id){
                   HomeIds.CREATE_CIRCLE -> {
                       //TODO
                   }
                   HomeIds.JOIN_CIRCLE -> {
                       //TODO
                   }
                   HomeIds.ADD_TO_CIRCLE -> {
                       //TODO
                   }
                   HomeIds.MEMBER_DETAIL -> {
                       //TODO
                   }
               }
            }
        }
    }
}

enum class HomeIds{
    CREATE_CIRCLE,
    JOIN_CIRCLE,
    ADD_TO_CIRCLE,
    MEMBER_DETAIL
}