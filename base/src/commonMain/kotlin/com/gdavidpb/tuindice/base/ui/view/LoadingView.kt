package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun LoadingView(
	modifier: Modifier = Modifier,
	indicatorTag: String? = null
) {
	Box(modifier = modifier.fillMaxSize()) {
		CircularProgressIndicator(
			modifier = Modifier
				.align(Alignment.Center)
				.then(
					if (indicatorTag != null) Modifier.testTag(indicatorTag) else Modifier
				)
		)
	}
}
