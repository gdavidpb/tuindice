package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.presentation.contract.SignIn

@Composable
fun SignInLoggingInView(state: SignIn.State) {
	if (state !is SignIn.State.LoggingIn) return

	Column(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Image(
			modifier = Modifier
				.padding(
					vertical = dimensionResource(id = R.dimen.dp_32)
				),
			painter = painterResource(id = R.drawable.ic_launcher),
			contentDescription = null
		)

		RandomFlipperText(items = state.messages)
	}
}