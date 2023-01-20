package com.gdavidpb.tuindice.base.domain.model

sealed class StartUpAction {
	data class Main(val screen: Int) : StartUpAction()
	object SignIn : StartUpAction()
}