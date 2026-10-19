package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The chat's notices («Reporte enviado», «Copiado»…) show here, inside the sheet, right above the composer. The
// screen under the sheet has a host of its own, which the sheet covers, so its notices would stay hidden until the
// sheet closed. The snackbar takes the theme's colours, inverseSurface with inverseOnSurface, as the app's snackbar
// does. Its bottom space is part of the snackbar, so the host takes no room while no notice shows.
@Composable
fun AssistantChatSnackbarView(
	hostState: SnackbarHostState,
	modifier: Modifier = Modifier
) {
	SnackbarHost(
		hostState = hostState,
		modifier = modifier.fillMaxWidth().testTag(AssistantUiTags.ChatSnackbar)
	) { data ->
		Snackbar(modifier = Modifier.padding(bottom = TuIndiceSpacing.Small)) {
			Text(
				modifier = Modifier.testTag(AssistantUiTags.ChatSnackbarMessage),
				text = data.visuals.message
			)
		}
	}
}
