package com.kgjr.safecircle.ui.utils.Auth.google_sign_in

data class SignInResult(
    val data: CurrentUserData?,
    val errorMessenger: String?
)

data class CurrentUserData(
    val userId: String,
    val userName: String,
    val email: String?,
    val profileUrl: String?
)