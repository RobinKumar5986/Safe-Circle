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
import com.kgjr.safecircle.ui.layouts.PlaceCheckInScreen
import com.kgjr.safecircle.ui.navigationGraph.NavigationDestinations

fun NavGraphBuilder.homeScreenGraph(navController: NavController){
    navigation(
        route = NavigationDestinations.homeScreenMain, //@Note: rout for the main graph
        startDestination = NavigationDestinations.homeScreen //Starting destination
    ) {
        composable(NavigationDestinations.homeScreen) {
            GroupScreen{ navId, dynamicId ->
                when (navId) {
                    HomeIds.ADD_TO_CIRCLE -> {
                        navController.navigate("INVITATION_SCREEN/$dynamicId")
                    }
                    HomeIds.LOCATION_HISTORY -> {
                        navController.navigate("LOCATION_HISTORY/$dynamicId")
                    }
                    HomeIds.LOCATION_CHECKING_IN_PLACE -> {
                        navController.navigate(NavigationDestinations.locationCheckIn)
                    }
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
        composable(NavigationDestinations.locationCheckIn){
            PlaceCheckInScreen(){
                navController.popBackStack()
            }
        }
    }
}

enum class HomeIds{
    ADD_TO_CIRCLE,
    LOCATION_HISTORY,
    LOCATION_CHECKING_IN_PLACE
}