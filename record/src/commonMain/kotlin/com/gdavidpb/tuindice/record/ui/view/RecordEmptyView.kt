package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.view.IllustratedMessageView
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.painterResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.il_record_empty

@Composable
fun RecordEmptyView(
	title: String,
	message: String
) {
	Surface(
		modifier = Modifier
			.testTag(RecordUiTags.EmptyContainer)
			.fillMaxSize(),
		color = MaterialTheme.colorScheme.background,
		contentColor = MaterialTheme.colorScheme.onBackground
	) {
		IllustratedMessageView(
			modifier = Modifier
				.fillMaxSize()
				.padding(24.dp),
			title = title,
			message = message,
			titleTestTag = RecordUiTags.EmptyTitle,
			messageTestTag = RecordUiTags.EmptyMessage,
			verticalArrangement = Arrangement.Center,
			headerContent = {
				Image(
					modifier = Modifier
						.testTag(RecordUiTags.EmptyIllustration)
						.padding(bottom = 32.dp),
					painter = painterResource(Res.drawable.il_record_empty),
					contentDescription = null
				)
			}
		)
	}
}
