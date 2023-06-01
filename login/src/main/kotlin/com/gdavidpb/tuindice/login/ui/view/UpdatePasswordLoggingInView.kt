package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun UpdatePasswordLoggingInView() {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		CircularProgressIndicator(
			modifier = Modifier
				.align(Alignment.CenterHorizontally)
		)
	}
}