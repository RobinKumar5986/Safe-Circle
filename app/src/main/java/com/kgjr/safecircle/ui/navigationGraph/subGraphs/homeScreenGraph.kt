package com.kgjr.safecircle.ui.navigationGraph.subGraphs

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.ui.layouts.GroupScreen
import com.kgjr.safecircle.ui.layouts.InviteCodeScreen
import com.kgjr.safecircle.ui.navigationGraph.NavigationDestinations

fun NavGraphBuilder.homeScreenGraph(navController: NavController){
    navigation(
        route = NavigationDestinations.homeScreenMain, //@Note: rout for the main graph
        startDestination = NavigationDestinations.homeScreen //Starting destination
    ) {
        composable(NavigationDestinations.homeScreen) {
            GroupScreen{ navId, groupId ->
                if(navId ==  HomeIds.ADD_TO_CIRCLE ){
                    navController.navigate("INVITATION_SCREEN/$groupId")
                }
            }
        }
        composable(NavigationDestinations.invitationScreen,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            InviteCodeScreen(code = groupId){
                navController.popBackStack()
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