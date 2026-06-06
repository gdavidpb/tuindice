package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_search_example_algorithms
import tuindice.subjects.generated.resources.subjects_search_example_math
import tuindice.subjects.generated.resources.subjects_search_example_physics
import tuindice.subjects.generated.resources.subjects_search_examples_label
import tuindice.subjects.generated.resources.subjects_search_min_query_message
import tuindice.subjects.generated.resources.subjects_search_min_query_title

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubjectSearchGuidanceView(
	query: String,
	onExampleClick: (String) -> Unit,
	modifier: Modifier = Modifier
) {
	val hasDraftQuery = query.trim().isNotEmpty()
	val examples = listOf(
		stringResource(Res.string.subjects_search_example_math),
		stringResource(Res.string.subjects_search_example_physics),
		stringResource(Res.string.subjects_search_example_algorithms)
	)

	Column(
		modifier = modifier
			.fillMaxWidth()
			.testTag(SubjectsUiTags.SearchGuidance),
		verticalArrangement = Arrangement.spacedBy(14.dp)
	) {
		if (hasDraftQuery) {
			Text(
				text = stringResource(Res.string.subjects_search_min_query_title),
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onSurface
			)
			Text(
				text = stringResource(Res.string.subjects_search_min_query_message),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		Text(
			text = stringResource(Res.string.subjects_search_examples_label),
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
					modifier = Modifier.testTag(SubjectsUiTags.searchExample(index)),
					onClick = { onExampleClick(example) },
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
