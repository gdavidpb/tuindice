package com.gdavidpb.tuindice.domain.model

sealed class StartUpAction {
    data class Main(val screen: Int, val account: Account) : StartUpAction()
    object SignIn : StartUpAction()
}