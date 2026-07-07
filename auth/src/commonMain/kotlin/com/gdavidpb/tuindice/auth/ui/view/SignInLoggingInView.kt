package com.gdavidpb.tuindice.auth.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.ui.view.AppLogoView
import kotlinx.coroutines.delay

// Fast logins never see the cancel affordance: it only fades in at the bottom
// once the wait is long enough to need an exit.
private const val CANCEL_REVEAL_DELAY_MILLIS = 4_000L

@Composable
fun SignInLoggingInView(
	state: SignIn.State.LoggingIn,
	cancelText: String? = null,
	onCancelClick: (() -> Unit)? = null
) {
	Box(
		modifier = Modifier
			.testTag(AuthUiTags.SignInLoggingInContainer)
			.fillMaxSize()
	) {
		Column(
			modifier = Modifier.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			AppLogoView(
				modifier = Modifier.padding(vertical = 32.dp),
				contentDescription = null
			)

			RandomFlipperText(items = state.messages)
		}

		if (cancelText != null && onCancelClick != null) {
			val isCancelVisible = remember { mutableStateOf(false) }

			LaunchedEffect(Unit) {
				delay(CANCEL_REVEAL_DELAY_MILLIS)
				isCancelVisible.value = true
			}

			AnimatedVisibility(
				modifier = Modifier
					.align(Alignment.BottomCenter)
					.padding(bottom = 24.dp),
				visible = isCancelVisible.value,
				enter = fadeIn()
			) {
				OutlinedButton(
					modifier = Modifier.testTag(AuthUiTags.SignInCancelButton),
					onClick = onCancelClick,
					border = null
				) {
					Text(text = cancelText)
				}
			}
		}
	}
}
