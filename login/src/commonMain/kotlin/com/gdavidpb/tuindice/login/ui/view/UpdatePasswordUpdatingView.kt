package com.gdavidpb.tuindice.login.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.login.ui.LoginUiTags

@Composable
fun UpdatePasswordUpdatingView() {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		CircularProgressIndicator(
			modifier = Modifier
				.testTag(LoginUiTags.UpdatePasswordUpdatingIndicator)
				.align(Alignment.CenterHorizontally)
		)
	}
}
