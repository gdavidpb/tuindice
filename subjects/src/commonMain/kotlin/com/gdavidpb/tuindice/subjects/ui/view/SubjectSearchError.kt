package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_message_retry
import tuindice.subjects.generated.resources.subjects_search_error_message
import tuindice.subjects.generated.resources.subjects_search_error_title

@Composable
fun SubjectSearchError(onRetryClick: () -> Unit) {
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.Start
	) {
		SubjectSearchMessage(
			title = stringResource(Res.string.subjects_search_error_title),
			description = stringResource(Res.string.subjects_search_error_message)
		)
		Spacer(modifier = Modifier.height(16.dp))
		Button(
			modifier = Modifier.testTag(SubjectsUiTags.SearchRetry),
			onClick = onRetryClick
		) {
			Text(text = stringResource(Res.string.subjects_message_retry))
		}
	}
}
