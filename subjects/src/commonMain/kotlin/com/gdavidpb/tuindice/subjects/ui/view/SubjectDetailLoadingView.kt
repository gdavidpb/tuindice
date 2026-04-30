package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.base.ui.view.IllustratedMessageView
import com.gdavidpb.tuindice.base.ui.view.StatsLoadingAnimationView
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags

@Composable
fun SubjectDetailLoadingView(
	title: String,
	message: String
) {
	IllustratedMessageView(
		modifier = Modifier
			.fillMaxSize()
			.padding(top = InternalScreenDefaults.TopBarSpacing)
			.padding(horizontal = 24.dp)
			.testTag(SubjectsUiTags.Loading),
		title = title,
		message = message,
		titleTestTag = SubjectsUiTags.LoadingTitle,
		messageTestTag = SubjectsUiTags.LoadingMessage,
		verticalArrangement = Arrangement.Center,
		headerContent = {
			StatsLoadingAnimationView()
		}
	)
}
