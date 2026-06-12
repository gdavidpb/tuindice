package com.gdavidpb.tuindice.record.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.exposeTestTagsAsResourceId
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.term_selection_mode_historical_description
import tuindice.record.generated.resources.term_selection_mode_projection_description
import tuindice.record.generated.resources.term_selection_selected_content_description
import tuindice.record.generated.resources.term_selection_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermSelectionBottomSheet(
	terms: List<TermItem>,
	selectedTermId: String,
	viewMode: RecordViewMode,
	onTermSelected: (termId: String) -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val termsByYear = terms
		.sortedByDescending(TermItem::termOrder)
		.groupBy(TermItem::periodYear)
	val modeDescriptionText = when (viewMode) {
		RecordViewMode.Historical -> stringResource(Res.string.term_selection_mode_historical_description)
		RecordViewMode.Projection -> stringResource(Res.string.term_selection_mode_projection_description)
	}
	val selectedContentDescription = stringResource(Res.string.term_selection_selected_content_description)

	ModalBottomSheet(
		modifier = Modifier
			.exposeTestTagsAsResourceId()
			.testTag(RecordUiTags.TermSelectionSheet),
		sheetState = sheetState,
		onDismissRequest = onDismissRequest
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(
					start = 24.dp,
					end = 24.dp,
					bottom = 24.dp
				),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
				Text(
					text = stringResource(Res.string.term_selection_title),
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Bold
				)
				Text(
					text = modeDescriptionText,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}

			LazyColumn(
				modifier = Modifier
					.fillMaxWidth()
					.heightIn(max = 520.dp)
					.testTag(RecordUiTags.TermSelectionList),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				termsByYear.forEach { (year, yearTerms) ->
					item(key = "term_selection_year_$year") {
						TermSelectionYearHeaderView(year = year)
					}

					items(
						items = yearTerms,
						key = TermItem::termId,
						contentType = { TermSelectionOptionContentType }
					) { term ->
						TermSelectionTermRowView(
							term = term,
							isSelected = term.termId == selectedTermId,
							selectedContentDescription = selectedContentDescription,
							onClick = {
								onTermSelected(term.termId)
								onDismissRequest()
							}
						)
					}
				}
			}
		}
	}
}

private const val TermSelectionOptionContentType = "term_selection_option"
