package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectSearchResultItem
import org.jetbrains.compose.resources.stringResource
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_search_no_results
import tuindice.subjects.generated.resources.subjects_search_results

@Composable
fun SubjectSearchResults(
	query: String,
	results: List<SubjectSearchResultItem>,
	isRefreshing: Boolean,
	onSubjectClick: (String) -> Unit
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = if (results.isEmpty() && !isRefreshing) {
				stringResource(Res.string.subjects_search_no_results, query)
			} else {
				stringResource(Res.string.subjects_search_results, results.size, query)
			},
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
		if (isRefreshing) {
			CircularProgressIndicator(
				modifier = Modifier.size(18.dp),
				strokeWidth = 2.dp
			)
		}
	}
	Spacer(modifier = Modifier.height(16.dp))
	LazyColumn(
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		items(
			items = results,
			key = SubjectSearchResultItem::subjectCode
		) { item ->
			SubjectSearchResultCard(
				item = item,
				onClick = { onSubjectClick(item.subjectCode) }
			)
		}
	}
}
