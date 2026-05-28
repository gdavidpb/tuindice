package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.ui.view.EmptyStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.EmptyView
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_empty_message
import tuindice.pensum.generated.resources.pensum_empty_title

@Composable
fun PensumEmptyView() {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(ScreenBackground)
	) {
		EmptyView(
			title = stringResource(Res.string.pensum_empty_title),
			message = stringResource(Res.string.pensum_empty_message),
			headerContent = { EmptyStateAnimationView() }
		)
	}
}
