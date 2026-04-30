package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.base.ui.view.StatsLoadingAnimationView
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags

@Composable
fun SubjectDetailLoadingView() {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.padding(top = InternalScreenDefaults.TopBarSpacing)
			.testTag(SubjectsUiTags.Loading),
		contentAlignment = Alignment.Center
	) {
		StatsLoadingAnimationView()
	}
}
