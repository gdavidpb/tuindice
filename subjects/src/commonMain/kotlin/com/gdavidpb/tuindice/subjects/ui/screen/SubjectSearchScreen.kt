package com.gdavidpb.tuindice.subjects.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectSearchResultItem
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_message_retry
import tuindice.subjects.generated.resources.subjects_search_clear_content_description
import tuindice.subjects.generated.resources.subjects_search_error
import tuindice.subjects.generated.resources.subjects_search_no_results
import tuindice.subjects.generated.resources.subjects_search_placeholder
import tuindice.subjects.generated.resources.subjects_search_result_content_description
import tuindice.subjects.generated.resources.subjects_search_results

@Composable
fun SubjectSearchScreen(
	state: SubjectSearch.State,
	onQueryChange: (String) -> Unit,
	onClearClick: () -> Unit,
	onRetryClick: () -> Unit,
	onSubjectClick: (String) -> Unit,
	modifier: Modifier = Modifier
) {
	val focusRequester = remember { FocusRequester() }

	LaunchedEffect(Unit) {
		focusRequester.requestFocus()
	}

	Column(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
			.padding(horizontal = 20.dp, vertical = 20.dp)
			.testTag(SubjectsUiTags.SearchScreen)
	) {
		SubjectSearchTextField(
			query = state.query,
			focusRequester = focusRequester,
			onQueryChange = onQueryChange,
			onClearClick = onClearClick
		)
		Spacer(modifier = Modifier.height(28.dp))

		when {
			state.query.trim().length < 2 ->
				Unit

			state.hasRemoteError ->
				SubjectSearchError(
					onRetryClick = onRetryClick
				)

			else ->
				SubjectSearchResults(
					query = state.query,
					results = state.results,
					isRefreshing = state.isRefreshing,
					onSubjectClick = onSubjectClick
				)
		}
	}
}

@Composable
private fun SubjectSearchTextField(
	query: String,
	focusRequester: FocusRequester,
	onQueryChange: (String) -> Unit,
	onClearClick: () -> Unit
) {
	OutlinedTextField(
		modifier = Modifier
			.fillMaxWidth()
			.focusRequester(focusRequester)
			.testTag(SubjectsUiTags.SearchTextField),
		value = query,
		onValueChange = onQueryChange,
		singleLine = true,
		shape = RoundedCornerShape(16.dp),
		textStyle = MaterialTheme.typography.bodyLarge,
		placeholder = {
			Text(
				text = stringResource(Res.string.subjects_search_placeholder),
				style = MaterialTheme.typography.bodyLarge,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		},
		leadingIcon = {
			Icon(
				imageVector = Icons.Outlined.Search,
				contentDescription = null
			)
		},
		trailingIcon = {
			if (query.isNotEmpty()) {
				IconButton(
					modifier = Modifier.testTag(SubjectsUiTags.SearchClear),
					onClick = onClearClick
				) {
					Icon(
						imageVector = Icons.Outlined.Close,
						contentDescription = stringResource(Res.string.subjects_search_clear_content_description)
					)
				}
			}
		},
		keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
		keyboardActions = KeyboardActions(onSearch = {}),
		colors = OutlinedTextFieldDefaults.colors(
			focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f),
			unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f)
		)
	)
}

@Composable
private fun SubjectSearchResults(
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

@Composable
private fun SubjectSearchResultCard(
	item: SubjectSearchResultItem,
	onClick: () -> Unit
) {
	val codeColors = remember(item.subjectCode) {
		CourseCodeColorGenerator.fromCode(item.subjectCode)
	}
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.testTag(SubjectsUiTags.searchResult(item.subjectCode))
			.clickable(onClick = onClick),
		shape = RoundedCornerShape(14.dp),
		color = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f),
		border = androidx.compose.foundation.BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				modifier = Modifier
					.background(codeColors.containerColor, RoundedCornerShape(10.dp))
					.padding(horizontal = 12.dp, vertical = 8.dp),
				text = item.subjectCode,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				color = codeColors.color,
				maxLines = 1
			)
			Column(
				modifier = Modifier
					.weight(1f)
					.padding(start = 16.dp, end = 12.dp)
			) {
				Text(
					text = item.name,
					style = MaterialTheme.typography.bodyLarge,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis
				)
				Spacer(modifier = Modifier.height(8.dp))
				Text(
					text = item.creditsText,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			Icon(
				imageVector = Icons.Outlined.ChevronRight,
				contentDescription = stringResource(
					Res.string.subjects_search_result_content_description,
					item.subjectCode
				),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

@Composable
private fun SubjectSearchError(onRetryClick: () -> Unit) {
	Column(
		modifier = Modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			text = stringResource(Res.string.subjects_search_error),
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
		Spacer(modifier = Modifier.height(16.dp))
		Button(
			modifier = Modifier.testTag(SubjectsUiTags.SearchRetry),
			onClick = onRetryClick
		) {
			Text(text = stringResource(Res.string.subjects_message_retry))
		}
	}
}
