package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.BaseUiTags

@Composable
fun EmptyView(
	title: String,
	message: String,
	actionLabel: String? = null,
	onActionClick: () -> Unit = {},
	headerContent: @Composable () -> Unit = {}
) {
	IllustratedMessageView(
		modifier = Modifier
			.testTag(BaseUiTags.EmptyViewContainer)
			.padding(horizontal = 24.dp)
			.fillMaxSize(),
		title = title,
		message = message,
		actionLabel = actionLabel,
		onActionClick = onActionClick,
		titleTestTag = BaseUiTags.EmptyViewTitle,
		messageTestTag = BaseUiTags.EmptyViewMessage,
		actionTestTag = BaseUiTags.EmptyViewActionButton,
		headerContent = { headerContent() }
	)
}
