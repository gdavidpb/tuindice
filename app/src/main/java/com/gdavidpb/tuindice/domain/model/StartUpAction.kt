package com.gdavidpb.tuindice.domain.model

sealed class StartUpAction {
    data class Main(val screen: Int, val account: Account) : StartUpAction()
    data class ResetPassword(val email: String) : StartUpAction()
    object SignIn : StartUpAction()
}