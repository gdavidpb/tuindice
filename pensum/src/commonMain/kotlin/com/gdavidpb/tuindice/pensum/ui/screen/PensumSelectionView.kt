package com.gdavidpb.tuindice.pensum.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_selection_apply
import tuindice.pensum.generated.resources.pensum_selection_cancel
import tuindice.pensum.generated.resources.pensum_selection_current
import tuindice.pensum.generated.resources.pensum_selection_modality
import tuindice.pensum.generated.resources.pensum_selection_title
import tuindice.pensum.generated.resources.pensum_selection_version

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PensumSelectionBottomSheet(
	model: PensumScreenModel,
	onSelectionApplied: (PensumScreenModel.PensumOptionItem, PensumScreenModel.ModalityItem) -> Unit,
	onDismissRequest: () -> Unit
) {
	val currentPensum = model.selectedPensumOption() ?: model.pensumOptions.firstOrNull()
	val currentModality = model.selectedModality() ?: model.modalityOptions.firstOrNull()
	if (currentPensum == null || currentModality == null) return

	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val selectedPensumState = remember(currentPensum.id) {
		mutableStateOf(currentPensum)
	}
	val selectedModalityState = remember(currentModality.id) {
		mutableStateOf(currentModality)
	}
	val currentSelectionText = stringResource(
		Res.string.pensum_selection_current,
		currentPensum.careerName,
		currentPensum.year,
		currentModality.name
	)

	ConfirmationDialog(
		sheetState = sheetState,
		titleText = stringResource(Res.string.pensum_selection_title),
		positiveText = stringResource(Res.string.pensum_selection_apply),
		negativeText = stringResource(Res.string.pensum_selection_cancel),
		onPositiveClick = {
			val selectedPensum = selectedPensumState.value
			val selectedModality = selectedModalityState.value
			val isSelectionChanged =
				selectedPensum.careerCode != model.selection.careerCode ||
					selectedPensum.year != model.selection.year ||
					selectedModality.id != model.selection.modalityId

			if (isSelectionChanged) {
				onSelectionApplied(selectedPensum, selectedModality)
			}
		},
		onDismissRequest = onDismissRequest
	) {
		Column(
			modifier = Modifier.fillMaxWidth(),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Text(
				text = currentSelectionText,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)

			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				Text(
					text = stringResource(Res.string.pensum_selection_version),
					style = MaterialTheme.typography.titleSmall,
					fontWeight = FontWeight.SemiBold
				)
				LazyRow(
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					items(
						items = model.pensumOptions,
						key = PensumScreenModel.PensumOptionItem::id
					) { item ->
						FilterChip(
							selected = item.id == selectedPensumState.value.id,
							onClick = { selectedPensumState.value = item },
							label = {
								Text(
									text = item.year.toString(),
									maxLines = 1
								)
							},
							colors = FilterChipDefaults.filterChipColors(
								selectedContainerColor = Current.copy(alpha = 0.18f),
								selectedLabelColor = MaterialTheme.colorScheme.onSurface
							)
						)
					}
				}
			}

			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				Text(
					text = stringResource(Res.string.pensum_selection_modality),
					style = MaterialTheme.typography.titleSmall,
					fontWeight = FontWeight.SemiBold
				)
				model.modalityOptions.forEach { modality ->
					PensumModalityOptionRow(
						modality = modality,
						isSelected = modality.id == selectedModalityState.value.id,
						onClick = { selectedModalityState.value = modality }
					)
				}
			}
		}
	}
}

@Composable
private fun PensumModalityOptionRow(
	modality: PensumScreenModel.ModalityItem,
	isSelected: Boolean,
	onClick: () -> Unit
) {
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick),
		shape = RoundedCornerShape(8.dp),
		color = if (isSelected)
			MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
		else
			MaterialTheme.colorScheme.surface,
		border = BorderStroke(
			width = 1.dp,
			color = if (isSelected)
				MaterialTheme.colorScheme.primary
			else
				MaterialTheme.colorScheme.outlineVariant
		)
	) {
		Row(
			modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Text(
				modifier = Modifier.weight(1f),
				text = modality.name,
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			if (isSelected) {
				Icon(
					imageVector = Icons.Filled.Check,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary
				)
			}
		}
	}
}

private fun PensumScreenModel.selectedPensumOption(): PensumScreenModel.PensumOptionItem? {
	return pensumOptions.firstOrNull { option ->
		option.careerCode == selection.careerCode && option.year == selection.year
	}
}

private fun PensumScreenModel.selectedModality(): PensumScreenModel.ModalityItem? {
	return modalityOptions.firstOrNull { modality -> modality.id == selection.modalityId }
}
