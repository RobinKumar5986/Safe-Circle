package com.kgjr.safecircle.ui.utils.Auth.google_sign_in

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SignInViewModel: ViewModel() {
    private val _state =  MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun onSignInResult(result: SignInResult){
        _state.update { it.copy(
            isSignInSuccessFull = result.data != null,
            signInErrorMessage = result.errorMessenger
        ) }
    }

    fun resetState(){
        _state.update { SignInState() }
    }
}