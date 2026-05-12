package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
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
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailability
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_button
import tuindice.record.generated.resources.create_term_load_prefix
import tuindice.record.generated.resources.create_term_no_suggestions
import tuindice.record.generated.resources.create_term_period_placeholder
import tuindice.record.generated.resources.create_term_search_error
import tuindice.record.generated.resources.create_term_search_placeholder
import tuindice.record.generated.resources.create_term_search_results
import tuindice.record.generated.resources.create_term_selected_count
import tuindice.record.generated.resources.create_term_selected_title
import tuindice.record.generated.resources.create_term_suggested_title
import tuindice.record.generated.resources.create_term_subject_already_planned
import tuindice.record.generated.resources.create_term_subject_already_taken
import tuindice.record.generated.resources.create_term_subject_selected
import tuindice.record.generated.resources.create_term_subject_unavailable

@Composable
fun CreateSyntheticTermScreen(
	state: CreateSyntheticTerm.State,
	onQueryChange: (String) -> Unit,
	onClearQueryClick: () -> Unit,
	onPeriodSelected: (String) -> Unit,
	onSubjectAdd: (SyntheticTermSubject) -> Unit,
	onSubjectRemove: (String) -> Unit,
	onCreateClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	val focusRequester = remember { FocusRequester() }

	LazyColumn(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
			.padding(horizontal = 20.dp)
			.testTag(RecordUiTags.CreateSyntheticTermScreen),
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		item {
			Spacer(modifier = Modifier.height(InternalScreenDefaults.TopBarSpacing))
		}

		item {
			CreateTermPeriodRow(
				selectedPeriod = state.selectedPeriod,
				periodOptions = state.periodOptions,
				loadLabel = state.loadPreview?.band?.label,
				onPeriodSelected = onPeriodSelected
			)
		}

		item {
			Text(
				text = stringResource(
					Res.string.create_term_selected_count,
					state.selectedSubjects.size
				),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}

		item {
			CreateTermSearchField(
				query = state.query,
				focusRequester = focusRequester,
				onQueryChange = onQueryChange,
				onClearQueryClick = onClearQueryClick
			)
		}

		if (state.selectedSubjects.isNotEmpty()) {
			item {
				SectionTitle(text = stringResource(Res.string.create_term_selected_title))
			}
			items(
				items = state.selectedSubjects,
				key = SyntheticTermSubject::subjectCode
			) { subject ->
				CreateTermSubjectCard(
					subject = subject,
					action = SubjectCardAction.Remove,
					onClick = { onSubjectRemove(subject.subjectCode) }
				)
			}
		}

		when {
			state.query.trim().length >= 2 -> {
				item {
					SectionTitle(
						text = stringResource(
							Res.string.create_term_search_results,
							state.searchResults.size
						),
						isRefreshing = state.isRefreshingSearch
					)
				}

				if (state.hasSearchError) {
					item {
						Text(
							text = stringResource(Res.string.create_term_search_error),
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.error
						)
					}
				}

				items(
					items = state.searchResults,
					key = SyntheticTermSubject::subjectCode
				) { subject ->
					CreateTermSubjectCard(
						subject = subject,
						action = SubjectCardAction.Add,
						enabled = subject.canAdd,
						onClick = { onSubjectAdd(subject) }
					)
				}
			}

			else -> {
				item {
					SectionTitle(text = stringResource(Res.string.create_term_suggested_title))
				}
				if (state.suggestedSubjects.isEmpty()) {
					item {
						Text(
							text = stringResource(Res.string.create_term_no_suggestions),
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				} else {
					items(
						items = state.suggestedSubjects,
						key = SyntheticTermSubject::subjectCode
					) { subject ->
						CreateTermSubjectCard(
							subject = subject,
							action = SubjectCardAction.Add,
							enabled = subject.canAdd,
							onClick = { onSubjectAdd(subject) }
						)
					}
				}
			}
		}

		item {
			Button(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp, bottom = 28.dp)
					.testTag(RecordUiTags.CreateSyntheticTermSubmitButton),
				enabled = state.canCreate,
				onClick = onCreateClick
			) {
				Text(text = stringResource(Res.string.create_term_button))
			}
		}
	}
}

@Composable
private fun CreateTermPeriodRow(
	selectedPeriod: SyntheticTermPeriodOption?,
	periodOptions: List<SyntheticTermPeriodOption>,
	loadLabel: String?,
	onPeriodSelected: (String) -> Unit
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		val expanded = remember { mutableStateOf(false) }
		Box(modifier = Modifier.weight(1f)) {
			OutlinedButton(
				modifier = Modifier
					.fillMaxWidth()
					.testTag(RecordUiTags.CreateSyntheticTermPeriodSelector),
				onClick = { expanded.value = true },
				shape = RoundedCornerShape(14.dp)
			) {
				Text(
					modifier = Modifier.weight(1f),
					text = selectedPeriod?.label
						?: stringResource(Res.string.create_term_period_placeholder),
					style = MaterialTheme.typography.bodyLarge,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
				Icon(
					imageVector = Icons.Outlined.ArrowDropDown,
					contentDescription = null
				)
			}

			DropdownMenu(
				expanded = expanded.value,
				onDismissRequest = { expanded.value = false }
			) {
				periodOptions.forEach { option ->
					DropdownMenuItem(
						text = { Text(text = option.label) },
						onClick = {
							expanded.value = false
							onPeriodSelected(option.termKey)
						}
					)
				}
			}
		}

		if (loadLabel != null) {
			Surface(
				shape = RoundedCornerShape(999.dp),
				color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
				border = BorderStroke(
					width = 1.dp,
					color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
				)
			) {
				Text(
					modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
					text = stringResource(Res.string.create_term_load_prefix, loadLabel),
					style = MaterialTheme.typography.labelLarge,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onPrimaryContainer
				)
			}
		}
	}
}

@Composable
private fun CreateTermSearchField(
	query: String,
	focusRequester: FocusRequester,
	onQueryChange: (String) -> Unit,
	onClearQueryClick: () -> Unit
) {
	OutlinedTextField(
		modifier = Modifier
			.fillMaxWidth()
			.focusRequester(focusRequester)
			.testTag(RecordUiTags.CreateSyntheticTermSearchField),
		value = query,
		onValueChange = onQueryChange,
		singleLine = true,
		shape = RoundedCornerShape(16.dp),
		textStyle = MaterialTheme.typography.bodyMedium,
		placeholder = {
			Text(
				text = stringResource(Res.string.create_term_search_placeholder),
				style = MaterialTheme.typography.bodyMedium,
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
				IconButton(onClick = onClearQueryClick) {
					Icon(
						imageVector = Icons.Outlined.Close,
						contentDescription = null
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
private fun SectionTitle(
	text: String,
	isRefreshing: Boolean = false
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = text,
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onSurface
		)
		if (isRefreshing) {
			CircularProgressIndicator(
				modifier = Modifier.size(18.dp),
				strokeWidth = 2.dp
			)
		}
	}
}

@Composable
private fun CreateTermSubjectCard(
	subject: SyntheticTermSubject,
	action: SubjectCardAction,
	enabled: Boolean = true,
	onClick: () -> Unit
) {
	val codeColors = remember(subject.subjectCode) {
		CourseCodeColorGenerator.fromCode(subject.subjectCode)
	}
	val unavailableLabel = when {
		action == SubjectCardAction.Remove || enabled -> null
		subject.availability == SyntheticTermSubjectAvailability.SELECTED ->
			stringResource(Res.string.create_term_subject_selected)
		subject.availability == SyntheticTermSubjectAvailability.ALREADY_TAKEN ->
			stringResource(Res.string.create_term_subject_already_taken)
		subject.availability == SyntheticTermSubjectAvailability.ALREADY_PLANNED ->
			stringResource(Res.string.create_term_subject_already_planned)
		else ->
			stringResource(Res.string.create_term_subject_unavailable)
	}
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.testTag(RecordUiTags.createSyntheticTermSubject(subject.subjectCode)),
		shape = RoundedCornerShape(14.dp),
		color = MaterialTheme.colorScheme.surface.copy(alpha = 0.62f),
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 14.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				modifier = Modifier
					.background(codeColors.containerColor, RoundedCornerShape(10.dp))
					.padding(horizontal = 10.dp, vertical = 7.dp),
				text = subject.subjectCode,
				style = MaterialTheme.typography.bodySmall,
				fontWeight = FontWeight.Bold,
				color = codeColors.color,
				maxLines = 1
			)
			Column(
				modifier = Modifier
					.weight(1f)
					.padding(start = 14.dp, end = 8.dp)
			) {
				Text(
					text = subject.name,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis
				)
				Spacer(modifier = Modifier.height(6.dp))
				Text(
					text = subject.creditsText,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			if (unavailableLabel == null) {
				IconButton(onClick = onClick) {
					Icon(
						imageVector = when (action) {
							SubjectCardAction.Add -> Icons.Outlined.Add
							SubjectCardAction.Remove -> Icons.Outlined.DeleteOutline
						},
						contentDescription = null
					)
				}
			} else {
				Text(
					text = unavailableLabel,
					style = MaterialTheme.typography.labelMedium,
					fontWeight = FontWeight.SemiBold,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		}
	}
}

private enum class SubjectCardAction {
	Add,
	Remove
}
