package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.login.presentation.contract.SignIn

@Composable
fun SignInLoggingInView(
	state: SignIn.State,
	headerContent: @Composable () -> Unit = {}
) {
	if (state !is SignIn.State.LoggingIn) return

	Column(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		headerContent()
		RandomFlipperText(items = state.messages)
	}
}
