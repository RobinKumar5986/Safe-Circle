package com.kgjr.safecircle.ui.navigationGraph.subGraphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.kgjr.safecircle.ui.layouts.GroupScreen
import com.kgjr.safecircle.ui.layouts.InviteCodeScreen
import com.kgjr.safecircle.ui.layouts.LocationHistoryScreen
import com.kgjr.safecircle.ui.navigationGraph.NavigationDestinations

fun NavGraphBuilder.homeScreenGraph(navController: NavController){
    navigation(
        route = NavigationDestinations.homeScreenMain, //@Note: rout for the main graph
        startDestination = NavigationDestinations.homeScreen //Starting destination
    ) {
        composable(NavigationDestinations.homeScreen) {
            GroupScreen{ navId, dynamicId ->
                if(navId ==  HomeIds.ADD_TO_CIRCLE ){
                    navController.navigate("INVITATION_SCREEN/$dynamicId")
                }
                else if (navId == HomeIds.LOCATION_HISTORY){
                    navController.navigate("LOCATION_HISTORY/$dynamicId")
                }
            }
//            PushNotificationScreen()
        }
        composable(NavigationDestinations.invitationScreen,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            InviteCodeScreen(code = groupId){
                navController.popBackStack()
            }
        }
        composable(NavigationDestinations.locationHistoryScreen,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            LocationHistoryScreen(userId)

        }
    }
}

enum class HomeIds{
    ADD_TO_CIRCLE,
    LOCATION_HISTORY
}