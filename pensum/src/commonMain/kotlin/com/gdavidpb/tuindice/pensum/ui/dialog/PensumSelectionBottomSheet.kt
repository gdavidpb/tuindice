package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.view.Current
import com.gdavidpb.tuindice.pensum.ui.view.PensumModalityOptionRow
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_selection_apply
import tuindice.pensum.generated.resources.pensum_selection_cancel
import tuindice.pensum.generated.resources.pensum_selection_modality
import tuindice.pensum.generated.resources.pensum_selection_title
import tuindice.pensum.generated.resources.pensum_selection_version

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PensumSelectionBottomSheet(
	model: PensumScreenModel,
	onSelectionApplied: (PensumScreenModel.PensumOptionItem, PensumScreenModel.ModalityItem) -> Unit,
	onDismissRequest: () -> Unit
) {
	val currentPensum = model.selectedPensumOption() ?: model.pensumOptions.firstOrNull()
	val currentModality = currentPensum?.selectedModality(model.selection.modalityId)
	if (currentPensum == null || currentModality == null) return

	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val selectedPensumState = remember(currentPensum.id) {
		mutableStateOf(currentPensum)
	}
	val selectedModalityState = remember(currentModality.id) {
		mutableStateOf(currentModality)
	}
	val selectedPensumIndex = model.pensumOptions.indexOfFirst { option ->
		option.hasSameAcademicIdentity(currentPensum)
	}
	val versionListState = rememberLazyListState(
		initialFirstVisibleItemIndex = selectedPensumIndex.coerceAtLeast(0)
	)

	LaunchedEffect(currentPensum.careerCode, currentPensum.year, model.pensumOptions.size) {
		if (selectedPensumIndex >= 0) {
			versionListState.scrollToItem(selectedPensumIndex)
		}
	}

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
			Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
				Text(
					text = stringResource(Res.string.pensum_selection_version),
					style = MaterialTheme.typography.titleSmall,
					fontWeight = FontWeight.SemiBold
				)
				LazyRow(
					state = versionListState,
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					items(
						items = model.pensumOptions,
						key = PensumScreenModel.PensumOptionItem::id
					) { item ->
						FilterChip(
							modifier = Modifier.testTag(PensumUiTags.versionOption(item.careerCode, item.year)),
							selected = item.hasSameAcademicIdentity(selectedPensumState.value),
							onClick = {
								selectedPensumState.value = item
								selectedModalityState.value = item.selectedModality(selectedModalityState.value.id)
									?: item.modalityOptions.first()
							},
							label = {
								Text(
									text = item.text,
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
				selectedPensumState.value.modalityOptions.forEach { modality ->
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

private fun PensumScreenModel.PensumOptionItem.hasSameAcademicIdentity(
	other: PensumScreenModel.PensumOptionItem
): Boolean {
	return careerCode == other.careerCode && year == other.year
}

private fun PensumScreenModel.selectedPensumOption(): PensumScreenModel.PensumOptionItem? {
	return pensumOptions.firstOrNull { option ->
		option.careerCode == selection.careerCode && option.year == selection.year
	}
}

private fun PensumScreenModel.PensumOptionItem.selectedModality(
	modalityId: String
): PensumScreenModel.ModalityItem? {
	return modalityOptions.firstOrNull { modality -> modality.id == modalityId }
		?: modalityOptions.firstOrNull { modality -> modality.isDefault }
		?: modalityOptions.firstOrNull()
}
