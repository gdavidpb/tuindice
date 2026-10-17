package com.gdavidpb.tuindice.about.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.base.ui.exposeTestTagsAsResourceId
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.about_assistant_conversations_cancel
import tuindice.about.generated.resources.about_assistant_conversations_confirm
import tuindice.about.generated.resources.about_assistant_conversations_message
import tuindice.about.generated.resources.about_assistant_conversations_title

// Asks before deleting every conversation with the assistant; «Borrar» uses the error colors.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAssistantConversationsDialog(
	onConfirmClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState()

	ModalBottomSheet(
		modifier = Modifier
			.exposeTestTagsAsResourceId()
			.testTag(AboutUiTags.DeleteAssistantConversationsDialog),
		sheetState = sheetState,
		onDismissRequest = onDismissRequest
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(start = TuIndiceSpacing.Dialog, end = TuIndiceSpacing.Dialog, bottom = TuIndiceSpacing.Dialog)
		) {
			Text(
				text = stringResource(Res.string.about_assistant_conversations_title),
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Bold
			)
			Text(
				text = stringResource(Res.string.about_assistant_conversations_message),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.padding(top = TuIndiceSpacing.Medium)
			)
			Row(
				modifier = Modifier.fillMaxWidth().padding(top = TuIndiceSpacing.Dialog),
				horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium, Alignment.End)
			) {
				OutlinedButton(
					onClick = onDismissRequest,
					modifier = Modifier.testTag(AboutUiTags.DeleteAssistantConversationsCancel)
				) {
					Text(text = stringResource(Res.string.about_assistant_conversations_cancel))
				}
				Button(
					onClick = onConfirmClick,
					colors = ButtonDefaults.buttonColors(
						containerColor = MaterialTheme.colorScheme.error,
						contentColor = MaterialTheme.colorScheme.onError
					),
					modifier = Modifier.testTag(AboutUiTags.DeleteAssistantConversationsConfirm)
				) {
					Text(text = stringResource(Res.string.about_assistant_conversations_confirm))
				}
			}
		}
	}
}
