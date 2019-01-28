package com.gdavidpb.tuindice.domain.model

import com.gdavidpb.tuindice.domain.usecase.request.ResetRequest

sealed class StartUpAction {
    data class Main(val account: Account) : StartUpAction()
    data class Reset(val request: ResetRequest) : StartUpAction()
    data class AwaitingForReset(val email: String) : StartUpAction()
    data class AwaitingForVerify(val email: String) : StartUpAction()
    object Verified : StartUpAction()
    object Login : StartUpAction()
}