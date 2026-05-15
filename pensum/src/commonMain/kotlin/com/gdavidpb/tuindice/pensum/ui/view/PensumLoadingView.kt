package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun PensumLoadingView() {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(ScreenBackground)
	) {
		CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
	}
}
