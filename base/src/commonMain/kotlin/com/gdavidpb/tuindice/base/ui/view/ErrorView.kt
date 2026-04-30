package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.BaseUiTags

@Composable
fun ErrorView(
	title: String,
	message: String,
	retryText: String,
	onRetryClick: () -> Unit,
	headerContent: @Composable () -> Unit = {}
) {
	IllustratedMessageView(
		modifier = Modifier
			.testTag(BaseUiTags.ErrorViewContainer)
			.padding(horizontal = 24.dp)
			.fillMaxSize(),
		title = title,
		message = message,
		actionLabel = retryText,
		onActionClick = onRetryClick,
		titleTestTag = BaseUiTags.ErrorViewTitle,
		messageTestTag = BaseUiTags.ErrorViewMessage,
		actionTestTag = BaseUiTags.ErrorViewRetryButton,
		headerContent = { headerContent() }
	)
}
