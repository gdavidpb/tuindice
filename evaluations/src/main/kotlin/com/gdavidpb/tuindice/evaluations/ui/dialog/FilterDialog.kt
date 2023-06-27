package com.gdavidpb.tuindice.evaluations.ui.dialog

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationFilterSectionItem
import com.gdavidpb.tuindice.evaluations.ui.view.FilterView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
	sheetState: SheetState,
	items: List<EvaluationFilterSectionItem>,
	onFilterClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	ConfirmationDialog(
		sheetState = sheetState,
		titleText = stringResource(id = R.string.title_filter_evaluations),
		positiveText = stringResource(id = R.string.button_filter_evaluations),
		negativeText = stringResource(id = R.string.cancel),
		onPositiveClick = onFilterClick,
		onNegativeClick = onDismissRequest,
		onDismissRequest = onDismissRequest
	) {
		LazyColumn(
			modifier = Modifier
				.fillMaxHeight(fraction = 0.75f)
		) {
			itemsIndexed(items = items) { index, (name, icon, entries) ->
				FilterView(
					name = name,
					icon = icon,
					entries = entries,
					onEntrySelect = { filter, isSelected ->
						entries[filter]?.value = isSelected
					}
				)

				if (index != items.lastIndex)
					Divider(
						modifier = Modifier
							.padding(vertical = dimensionResource(id = R.dimen.dp_12))
					)
			}
		}
	}
}