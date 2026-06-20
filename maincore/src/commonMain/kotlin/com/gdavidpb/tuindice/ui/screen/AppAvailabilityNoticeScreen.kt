package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.IllustratedMessageView
import com.gdavidpb.tuindice.ui.MaincoreUiTags

@Composable
fun AppAvailabilityNoticeScreen(
	title: String,
	message: String
) {
	Surface(
		modifier = Modifier
			.testTag(MaincoreUiTags.AppAvailabilityNoticeScreen)
			.fillMaxSize(),
		color = MaterialTheme.colorScheme.background,
		contentColor = MaterialTheme.colorScheme.onBackground
	) {
		IllustratedMessageView(
			modifier = Modifier
				.fillMaxSize()
				.windowInsetsPadding(WindowInsets.systemBars)
				.padding(horizontal = 24.dp),
			title = title,
			message = message,
			verticalArrangement = Arrangement.Center,
			titleTestTag = MaincoreUiTags.AppAvailabilityNoticeTitle,
			messageTestTag = MaincoreUiTags.AppAvailabilityNoticeMessage,
			headerContent = { ErrorStateAnimationView() }
		)
	}
}
