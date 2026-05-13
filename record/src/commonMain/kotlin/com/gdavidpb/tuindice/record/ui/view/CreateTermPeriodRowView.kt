package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_period_placeholder

private val TermDropdownMaxHeight = 280.dp

@Composable
internal fun CreateTermPeriodRow(
	selectedPeriod: SyntheticTermPeriodOption?,
	periodOptions: List<SyntheticTermPeriodOption>,
	loadPreview: SyntheticTermLoadPreview?,
	hasSelectedSubjects: Boolean,
	isLoadingLoadPreview: Boolean,
	hasLoadPreviewError: Boolean,
	onPeriodSelected: (String) -> Unit
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		val expanded = remember { mutableStateOf(false) }
		Box(modifier = Modifier.weight(1f)) {
			OutlinedButton(
				modifier = Modifier
					.fillMaxWidth()
					.height(56.dp)
					.testTag(RecordUiTags.CreateSyntheticTermPeriodSelector),
				onClick = { expanded.value = true },
				shape = RoundedCornerShape(14.dp)
			) {
				Text(
					modifier = Modifier.weight(1f),
					text = selectedPeriod?.label
						?: stringResource(Res.string.create_term_period_placeholder),
					style = MaterialTheme.typography.bodyLarge,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
				Icon(
					imageVector = Icons.Outlined.ArrowDropDown,
					contentDescription = null
				)
			}

			DropdownMenu(
				modifier = Modifier.heightIn(max = TermDropdownMaxHeight),
				expanded = expanded.value,
				onDismissRequest = { expanded.value = false }
			) {
				periodOptions.forEach { option ->
					DropdownMenuItem(
						text = { Text(text = option.label) },
						onClick = {
							expanded.value = false
							onPeriodSelected(option.termKey)
						}
					)
				}
			}
		}

		CreateTermLoadChip(
			loadPreview = loadPreview,
			hasSelectedSubjects = hasSelectedSubjects,
			isLoading = isLoadingLoadPreview,
			hasError = hasLoadPreviewError
		)
	}
}
