package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

@Composable
fun ProfilePictureEditButton(
	modifier: Modifier = Modifier,
	isEnabled: Boolean,
	isUploading: Boolean,
	onClick: () -> Unit
) {
	IconButton(
		modifier = modifier
			.testTag(SummaryUiTags.ProfilePictureEditButton)
			.size(42.dp),
		enabled = isEnabled,
		colors = IconButtonDefaults.filledIconButtonColors(),
		onClick = onClick
	) {
		if (isUploading) {
			CircularProgressIndicator(
				modifier = Modifier
					.size(20.dp)
					.testTag(SummaryUiTags.ProfilePictureLoadingIndicator),
				color = MaterialTheme.colorScheme.onPrimary,
				strokeWidth = 2.dp
			)
		} else {
			Icon(
				modifier = Modifier
					.padding(4.dp),
				imageVector = Icons.Outlined.Edit,
				tint = MaterialTheme.colorScheme.onPrimary,
				contentDescription = null
			)
		}
	}
}
