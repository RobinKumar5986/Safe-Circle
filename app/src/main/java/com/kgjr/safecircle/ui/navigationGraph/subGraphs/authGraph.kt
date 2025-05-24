package com.kgjr.safecircle.ui.navigationGraph.subGraphs

import android.app.Activity.RESULT_OK
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.kgjr.safecircle.MainApplication
import com.kgjr.safecircle.ui.layouts.SignUpView
import com.kgjr.safecircle.ui.navigationGraph.NavigationDestinations
import com.kgjr.safecircle.ui.utils.LocationUtils
import com.kgjr.safecircle.ui.utils.NotificationUtils
import com.kgjr.safecircle.ui.utils.PhysicalActivityUtils
import com.kgjr.safecircle.ui.utils.Auth.google_sign_in.SignInViewModel
import kotlinx.coroutines.launch

fun NavGraphBuilder.authGraph(navController: NavController) {
    navigation(
        route = NavigationDestinations.loginScreenMain, //@Note: rout for the main graph
        startDestination = NavigationDestinations.loginScreen //Starting destination
    ) {
        composable(NavigationDestinations.loginScreen) {
            val viewMode = viewModel<SignInViewModel>()
            val state = viewMode.state.collectAsStateWithLifecycle().value
            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = { result ->
                    if (result.resultCode == RESULT_OK) {
                        val intent = result.data
                        coroutineScope.launch {
                            val signInResult = MainApplication.getGoogleAuthUiClient().signInWithIntent(
                                intent = intent ?: return@launch
                            )
                            viewMode.onSignInResult(signInResult)
                        }
                    }
                }
            )
            LaunchedEffect(state.isSignInSuccessFull) {
                if(state.isSignInSuccessFull == true){
                    Toast.makeText(context,"Success", Toast.LENGTH_SHORT).show()
                }
            }
            SignUpView(signInState = state,nav = {
                if(!LocationUtils.isLocationPermissionGranted(context) || !NotificationUtils.isNotificationPermissionGranted(context) || !PhysicalActivityUtils.isActivityPermissionGranted(context)) {
                    navController.navigate(NavigationDestinations.allPermissionMain) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }else{
                    navController.navigate(NavigationDestinations.homeScreenMain) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            }) {
                coroutineScope.launch {
                    val signInIntentSender = MainApplication.getGoogleAuthUiClient().signIn()
                    launcher.launch(
                        IntentSenderRequest.Builder(
                            signInIntentSender ?: return@launch
                        ).build()
                    )
                }
            }
        }

    }
}