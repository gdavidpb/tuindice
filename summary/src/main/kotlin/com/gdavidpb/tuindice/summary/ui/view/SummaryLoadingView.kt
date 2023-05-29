package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun SummaryLoadingView() {
	Box(modifier = Modifier.fillMaxSize()) {
		CircularProgressIndicator(
			modifier = Modifier.align(Alignment.Center)
		)
	}
}