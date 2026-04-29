package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.base.ui.view.AppLogoView

@Composable
fun SignInLoggingInView(state: SignIn.State.LoggingIn) {
	Column(
		modifier = Modifier
			.testTag(AuthUiTags.SignInLoggingInContainer)
			.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		AppLogoView(
			modifier = Modifier.padding(vertical = 32.dp),
			contentDescription = null
		)

		RandomFlipperText(items = state.messages)
	}
}
