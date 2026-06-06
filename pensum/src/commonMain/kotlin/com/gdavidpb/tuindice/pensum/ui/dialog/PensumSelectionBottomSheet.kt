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
import com.gdavidpb.tuindice.pensum.presentation.model.PensumModalityItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumOptionItem
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
	onSelectionApplied: (PensumOptionItem, PensumModalityItem) -> Unit,
	onDismissRequest: () -> Unit
) {
	val currentPensum = model.selectedPensumOption() ?: model.pensumOptions.firstOrNull()
	val currentModality = currentPensum?.selectedModality(model.selection.modalityId)
	if (currentPensum == null || currentModality == null) return

	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val selectedPensumState = remember(currentPensum.id) {
		mutableStateOf(currentPensum)
	}
	val selectedModalityIdState = remember(currentPensum.id, currentModality.id) {
		mutableStateOf(currentModality.id)
	}
	val selectedPensumIndex = model.pensumOptions.indexOfFirst { option ->
		option.hasSameAcademicIdentity(currentPensum)
	}
	val versionListState = rememberLazyListState(
		initialFirstVisibleItemIndex = selectedPensumIndex.coerceAtLeast(0)
	)

	LaunchedEffect(currentPensum.year, model.pensumOptions.size) {
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
			val selectedModality = checkNotNull(selectedPensum.selectedModality(selectedModalityIdState.value)) {
				"Expected selected modality to exist in the selected pensum."
			}
			val isSelectionChanged =
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
			PensumCurrentSelectionSummary(
				model = model,
				currentPensum = currentPensum,
				currentModality = currentModality
			)

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
						key = PensumOptionItem::id,
						contentType = { PensumVersionOptionContentType }
					) { item ->
						FilterChip(
							modifier = Modifier.testTag(PensumUiTags.versionOption(item.year)),
							selected = item.hasSameAcademicIdentity(selectedPensumState.value),
							onClick = {
								selectedPensumState.value = item
								selectedModalityIdState.value = (
									item.selectedModality(selectedModalityIdState.value)
										?: item.modalityOptions.first()
									).id
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
						modifier = Modifier.testTag(PensumUiTags.modalityOption(modality.id)),
						modality = modality,
						isSelected = modality.id == selectedModalityIdState.value,
						onClick = { selectedModalityIdState.value = modality.id }
					)
				}
			}
		}
	}
}

private const val PensumVersionOptionContentType = "pensum_version_option"

private fun PensumOptionItem.hasSameAcademicIdentity(
	other: PensumOptionItem
): Boolean {
	return year == other.year
}

private fun PensumScreenModel.selectedPensumOption(): PensumOptionItem? {
	return pensumOptions.firstOrNull { option ->
		option.year == selection.year
	}
}

private fun PensumOptionItem.selectedModality(
	modalityId: String
): PensumModalityItem? {
	return modalityOptions.firstOrNull { modality -> modality.id == modalityId }
		?: modalityOptions.firstOrNull { modality -> modality.isDefault }
		?: modalityOptions.firstOrNull()
}
