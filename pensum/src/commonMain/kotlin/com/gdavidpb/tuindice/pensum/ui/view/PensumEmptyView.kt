package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.ui.view.EmptyStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.EmptyView

@Composable
fun PensumEmptyView(
	title: String,
	message: String,
	actionLabel: String? = null,
	onActionClick: () -> Unit = {}
) {
	val graphColors = pensumGraphColors()
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(graphColors.screenBackground)
	) {
		EmptyView(
			title = title,
			message = message,
			actionLabel = actionLabel,
			onActionClick = onActionClick,
			headerContent = { EmptyStateAnimationView() }
		)
	}
}
