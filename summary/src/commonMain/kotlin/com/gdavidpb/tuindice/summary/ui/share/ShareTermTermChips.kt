package com.gdavidpb.tuindice.summary.ui.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.model.ShareTermTermItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * The closed terms as a row of chips, newest first. The term on the card is the one chip that is selected, and a tap on
 * any other one moves the card to it.
 */
@Composable
fun ShareTermTermChips(
	terms: List<ShareTermTermItem>,
	onTermClick: (termKey: String) -> Unit,
	modifier: Modifier = Modifier
) {
	LazyRow(
		modifier = modifier.testTag(SummaryUiTags.ShareTermTerms),
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		items(items = terms, key = { term -> term.key }) { term ->
			// The chosen term is marked on the box around its chip, a node of its own that the E2E driver reads.
			Box(
				modifier = if (term.selected) {
					Modifier.testTag(ShareTermUiTags.termSelected(term.key))
				} else {
					Modifier
				}
			) {
				FilterChip(
					selected = term.selected,
					onClick = { onTermClick(term.key) },
					label = { Text(text = term.label.asString()) },
					leadingIcon = if (term.selected) {
						{ SelectedTermCheck() }
					} else {
						null
					},
					modifier = Modifier.testTag(ShareTermUiTags.term(term.key))
				)
			}
		}
	}
}

@Composable
private fun SelectedTermCheck() {
	Icon(
		imageVector = Icons.Filled.Check,
		contentDescription = null,
		modifier = Modifier.size(18.dp)
	)
}
