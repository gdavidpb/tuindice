package com.gdavidpb.tuindice.domain.model

sealed class StartUpAction {
    object Main : StartUpAction()
    data class Reset(val email: String) : StartUpAction()
    data class Verify(val email: String) : StartUpAction()
    object Login : StartUpAction()
}