package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.view.IllustratedMessageView
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_loading_message
import tuindice.pensum.generated.resources.pensum_loading_title

@Composable
fun PensumLoadingView() {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(ScreenBackground)
	) {
		IllustratedMessageView(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 24.dp)
				.testTag(PensumUiTags.Loading),
			title = stringResource(Res.string.pensum_loading_title),
			message = stringResource(Res.string.pensum_loading_message),
			titleTestTag = PensumUiTags.LoadingTitle,
			messageTestTag = PensumUiTags.LoadingMessage,
			verticalArrangement = Arrangement.Center,
			headerContent = {
				PensumLoadingAnimationView()
			}
		)
	}
}
