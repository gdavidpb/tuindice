package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.label_delete_synthetic_term
import tuindice.record.generated.resources.label_edit_synthetic_term

@Composable
fun RecordSyntheticTermActionsView(
	termId: String,
	onEditClick: (String) -> Unit,
	onDeleteClick: (String) -> Unit,
	modifier: Modifier = Modifier
) {
	Column(modifier = modifier) {
		SmallFloatingActionButton(
			modifier = Modifier
				.padding(bottom = 4.dp)
				.testTag(RecordUiTags.EditSyntheticTermButton),
			containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
			contentColor = MaterialTheme.colorScheme.primaryContainer,
			onClick = { onEditClick(termId) }
		) {
			Icon(
				imageVector = Icons.Outlined.Edit,
				contentDescription = stringResource(Res.string.label_edit_synthetic_term)
			)
		}

		SmallFloatingActionButton(
			modifier = Modifier
				.padding(bottom = 16.dp)
				.testTag(RecordUiTags.DeleteSyntheticTermButton),
			containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
			contentColor = MaterialTheme.colorScheme.primaryContainer,
			onClick = { onDeleteClick(termId) }
		) {
			Icon(
				imageVector = Icons.Outlined.DeleteOutline,
				contentDescription = stringResource(Res.string.label_delete_synthetic_term)
			)
		}
	}
}
