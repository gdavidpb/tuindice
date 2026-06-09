package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_search_example_algorithms
import tuindice.record.generated.resources.create_term_search_example_math
import tuindice.record.generated.resources.create_term_search_example_physics
import tuindice.record.generated.resources.create_term_search_examples_label
import tuindice.record.generated.resources.create_term_search_min_query_message
import tuindice.record.generated.resources.create_term_search_min_query_title

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateTermSearchGuidance(
	query: String,
	onExampleClick: (String) -> Unit,
	modifier: Modifier = Modifier
) {
	val hasDraftQuery = query.trim().isNotEmpty()
	val examples = listOf(
		stringResource(Res.string.create_term_search_example_math),
		stringResource(Res.string.create_term_search_example_physics),
		stringResource(Res.string.create_term_search_example_algorithms)
	)

	Column(
		modifier = modifier
			.fillMaxWidth()
			.testTag(RecordUiTags.CreateSyntheticTermSearchGuidance),
		verticalArrangement = Arrangement.spacedBy(14.dp)
	) {
		if (hasDraftQuery) {
			CreateTermSearchMessage(
				title = stringResource(Res.string.create_term_search_min_query_title),
				description = stringResource(Res.string.create_term_search_min_query_message)
			)
		}
		Text(
			text = stringResource(Res.string.create_term_search_examples_label),
			style = MaterialTheme.typography.labelLarge,
			fontWeight = FontWeight.SemiBold,
			color = MaterialTheme.colorScheme.onSurface
		)
		FlowRow(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			examples.forEachIndexed { index, example ->
				AssistChip(
					modifier = Modifier.testTag(RecordUiTags.createSyntheticTermSearchExample(index)),
					onClick = { onExampleClick(example) },
					colors = AssistChipDefaults.assistChipColors(
						containerColor = MaterialTheme.colorScheme.surfaceContainerLow
					),
					label = {
						Text(
							text = example,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
					}
				)
			}
		}
	}
}
