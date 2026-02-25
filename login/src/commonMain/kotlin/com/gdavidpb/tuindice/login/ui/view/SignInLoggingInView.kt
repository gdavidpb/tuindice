package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import org.jetbrains.compose.resources.painterResource
import tuindice.login.generated.resources.Res
import tuindice.login.generated.resources.ic_launcher

@Composable
fun SignInLoggingInView(state: SignIn.State.LoggingIn) {
	Column(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Image(
			modifier = Modifier.padding(vertical = 32.dp),
			painter = painterResource(Res.drawable.ic_launcher),
			contentDescription = null
		)

		RandomFlipperText(items = state.messages)
	}
}
