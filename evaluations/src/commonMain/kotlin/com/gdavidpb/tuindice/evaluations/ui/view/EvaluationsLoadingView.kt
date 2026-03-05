package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

@Composable
fun EvaluationsLoadingView() {
	Box(modifier = Modifier.fillMaxSize()) {
		CircularProgressIndicator(
			modifier = Modifier
				.testTag(EvaluationsUiTags.EvaluationsLoadingIndicator)
				.align(Alignment.Center)
		)
	}
}
