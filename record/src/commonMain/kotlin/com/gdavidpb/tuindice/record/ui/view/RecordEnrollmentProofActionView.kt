package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FindInPage
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
import tuindice.record.generated.resources.record_enrollment_proof_action

@Composable
fun RecordEnrollmentProofActionView(
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	SmallFloatingActionButton(
		modifier = modifier
			.padding(bottom = 16.dp)
			.testTag(RecordUiTags.EnrollmentProofButton),
		containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
		contentColor = MaterialTheme.colorScheme.primaryContainer,
		onClick = onClick
	) {
		Icon(
			imageVector = Icons.Outlined.FindInPage,
			contentDescription = stringResource(Res.string.record_enrollment_proof_action)
		)
	}
}
