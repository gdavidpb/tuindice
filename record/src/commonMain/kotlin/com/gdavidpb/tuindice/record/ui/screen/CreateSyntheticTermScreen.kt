package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadBand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailability
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_button
import tuindice.record.generated.resources.create_term_load_loading
import tuindice.record.generated.resources.create_term_load_prefix
import tuindice.record.generated.resources.create_term_load_placeholder
import tuindice.record.generated.resources.create_term_load_unavailable
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
import tuindice.record.generated.resources.create_term_subject_available
import tuindice.record.generated.resources.create_term_subject_requirement_pending
import tuindice.record.generated.resources.create_term_subject_requirements_met
import tuindice.record.generated.resources.create_term_subject_selected

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

	Box(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
			.testTag(RecordUiTags.CreateSyntheticTermScreen)
	) {
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 20.dp),
			contentPadding = PaddingValues(bottom = 116.dp),
			verticalArrangement = Arrangement.spacedBy(20.dp)
		) {
			item {
				Spacer(modifier = Modifier.height(InternalScreenDefaults.TopBarSpacing))
			}

			item {
				CreateTermPeriodRow(
					selectedPeriod = state.selectedPeriod,
					periodOptions = state.periodOptions,
					loadPreview = state.loadPreview,
					hasSelectedSubjects = state.selectedSubjects.isNotEmpty(),
					isLoadingLoadPreview = state.isLoadingLoadPreview,
					hasLoadPreviewError = state.hasLoadPreviewError,
					onPeriodSelected = onPeriodSelected
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
						CreateTermSelectedSubjectCard(
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
						item {
							LazyRow(
								horizontalArrangement = Arrangement.spacedBy(12.dp)
							) {
								items(
									items = state.suggestedSubjects,
									key = SyntheticTermSubject::subjectCode
								) { subject ->
									CreateTermSuggestedSubjectCard(
										subject = subject,
										enabled = subject.canAdd,
										onClick = { onSubjectAdd(subject) }
									)
								}
							}
						}
					}
				}
			}

			if (state.selectedSubjects.isNotEmpty()) {
				item {
					SectionTitle(text = stringResource(Res.string.create_term_selected_title))
				}
				items(
					items = state.selectedSubjects,
					key = SyntheticTermSubject::subjectCode
				) { subject ->
					CreateTermSelectedSubjectCard(
						subject = subject,
						action = SubjectCardAction.Remove,
						onClick = { onSubjectRemove(subject.subjectCode) }
					)
				}
			}
		}

		CreateTermSubmitBar(
			selectedCount = state.selectedSubjects.size,
			canCreate = state.canCreate,
			onCreateClick = onCreateClick,
			modifier = Modifier.align(Alignment.BottomCenter)
		)
	}
}

@Composable
private fun CreateTermSubmitBar(
	selectedCount: Int,
	canCreate: Boolean,
	onCreateClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier.fillMaxWidth(),
		color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)
		),
		tonalElevation = 6.dp
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 20.dp, vertical = 14.dp),
			horizontalArrangement = Arrangement.spacedBy(16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				modifier = Modifier
					.weight(1f)
					.widthIn(min = 148.dp),
				text = stringResource(Res.string.create_term_selected_count, selectedCount),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			Button(
				modifier = Modifier
					.width(162.dp)
					.height(52.dp)
					.testTag(RecordUiTags.CreateSyntheticTermSubmitButton),
				enabled = canCreate,
				onClick = onCreateClick,
				shape = RoundedCornerShape(999.dp)
			) {
				Text(
					text = stringResource(Res.string.create_term_button),
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		}
	}
}

@Composable
private fun CreateTermPeriodRow(
	selectedPeriod: SyntheticTermPeriodOption?,
	periodOptions: List<SyntheticTermPeriodOption>,
	loadPreview: SyntheticTermLoadPreview?,
	hasSelectedSubjects: Boolean,
	isLoadingLoadPreview: Boolean,
	hasLoadPreviewError: Boolean,
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
					.height(56.dp)
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
				modifier = Modifier.heightIn(max = TermDropdownMaxHeight),
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

		CreateTermLoadChip(
			loadPreview = loadPreview,
			hasSelectedSubjects = hasSelectedSubjects,
			isLoading = isLoadingLoadPreview,
			hasError = hasLoadPreviewError
		)
	}
}

@Composable
private fun CreateTermLoadChip(
	loadPreview: SyntheticTermLoadPreview?,
	hasSelectedSubjects: Boolean,
	isLoading: Boolean,
	hasError: Boolean
) {
	val status = when {
		isLoading -> LoadChipStatus.Loading
		!hasSelectedSubjects -> LoadChipStatus.Placeholder
		hasError -> LoadChipStatus.Unavailable
		loadPreview?.available == false -> LoadChipStatus.Unavailable
		loadPreview?.band != null -> LoadChipStatus.Available(loadPreview.band)
		else -> LoadChipStatus.Unavailable
	}
	val color = status.color()
	val label = status.label()

	Surface(
		modifier = Modifier
			.width(LoadChipWidth)
			.height(40.dp),
		shape = RoundedCornerShape(12.dp),
		color = color.copy(alpha = 0.12f),
		border = BorderStroke(
			width = 1.dp,
			color = color.copy(alpha = 0.9f)
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 12.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			if (status == LoadChipStatus.Loading) {
				CircularProgressIndicator(
					modifier = Modifier.size(12.dp),
					color = color,
					strokeWidth = 2.dp
				)
			} else {
				Box(
					modifier = Modifier
						.size(10.dp)
						.background(color, CircleShape)
				)
			}
			Text(
				modifier = Modifier.weight(1f),
				text = label,
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.SemiBold,
				color = color,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
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
private fun CreateTermSuggestedSubjectCard(
	subject: SyntheticTermSubject,
	enabled: Boolean,
	onClick: () -> Unit
) {
	Surface(
		modifier = Modifier
			.width(144.dp)
			.height(166.dp)
			.testTag(RecordUiTags.createSyntheticTermSubject(subject.subjectCode)),
		shape = RoundedCornerShape(14.dp),
		color = MaterialTheme.colorScheme.surface.copy(alpha = 0.56f),
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)
		)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(12.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.Top
			) {
				SubjectCodeChip(subjectCode = subject.subjectCode)
				SubjectActionButton(
					action = SubjectCardAction.Add,
					enabled = enabled,
					onClick = onClick
				)
			}
			Spacer(modifier = Modifier.height(18.dp))
			Text(
				text = subject.name,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis
			)
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = subject.creditsText,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Spacer(modifier = Modifier.weight(1f))
			SubjectStatusRow(
				subject = subject,
				availableText = stringResource(Res.string.create_term_subject_available),
				availableIcon = SubjectStatusIcon.Dot
			)
		}
	}
}

@Composable
private fun CreateTermSelectedSubjectCard(
	subject: SyntheticTermSubject,
	action: SubjectCardAction,
	enabled: Boolean = true,
	onClick: () -> Unit
) {
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.testTag(RecordUiTags.createSyntheticTermSubject(subject.subjectCode)),
		shape = RoundedCornerShape(14.dp),
		color = MaterialTheme.colorScheme.surface.copy(alpha = 0.56f),
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 14.dp, vertical = 14.dp),
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Column(modifier = Modifier.weight(1f)) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(12.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					SubjectCodeChip(subjectCode = subject.subjectCode)
					Text(
						modifier = Modifier.weight(1f),
						text = subject.name,
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onSurface,
						maxLines = 2,
						overflow = TextOverflow.Ellipsis
					)
				}
				Spacer(modifier = Modifier.height(8.dp))
				Text(
					text = subject.creditsText,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				Spacer(modifier = Modifier.height(8.dp))
				SubjectStatusRow(
					subject = subject,
					availableText = stringResource(Res.string.create_term_subject_requirements_met),
					availableIcon = SubjectStatusIcon.Check
				)
			}
			if (action == SubjectCardAction.Remove || enabled) {
				SubjectActionButton(
					action = action,
					enabled = enabled,
					onClick = onClick
				)
			}
		}
	}
}

@Composable
private fun SubjectCodeChip(subjectCode: String) {
	val codeColors = remember(subjectCode) {
		CourseCodeColorGenerator.fromCode(subjectCode)
	}
	Text(
		modifier = Modifier
			.background(codeColors.containerColor, RoundedCornerShape(8.dp))
			.padding(horizontal = 10.dp, vertical = 6.dp),
		text = subjectCode,
		style = MaterialTheme.typography.bodySmall,
		fontWeight = FontWeight.Bold,
		color = codeColors.color,
		maxLines = 1
	)
}

@Composable
private fun SubjectActionButton(
	action: SubjectCardAction,
	enabled: Boolean,
	onClick: () -> Unit
) {
	Surface(
		shape = RoundedCornerShape(10.dp),
		color = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f),
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
		)
	) {
		IconButton(
			modifier = Modifier.size(38.dp),
			enabled = enabled,
			onClick = onClick
		) {
			Icon(
				imageVector = when (action) {
					SubjectCardAction.Add -> Icons.Outlined.Add
					SubjectCardAction.Remove -> Icons.Outlined.DeleteOutline
				},
				contentDescription = null,
				tint = if (enabled)
					MaterialTheme.colorScheme.onSurface
				else
					MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
			)
		}
	}
}

@Composable
private fun SubjectStatusRow(
	subject: SyntheticTermSubject,
	availableText: String,
	availableIcon: SubjectStatusIcon
) {
	val status = subject.status(
		availableText = availableText,
		availableIcon = availableIcon
	)
	Row(
		horizontalArrangement = Arrangement.spacedBy(6.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		when (status.icon) {
			SubjectStatusIcon.Dot ->
				Box(
					modifier = Modifier
						.size(10.dp)
						.background(status.color, CircleShape)
				)

			SubjectStatusIcon.Check ->
				Icon(
					modifier = Modifier.size(16.dp),
					imageVector = Icons.Outlined.CheckCircleOutline,
					contentDescription = null,
					tint = status.color
				)

			SubjectStatusIcon.Clock ->
				Icon(
					modifier = Modifier.size(16.dp),
					imageVector = Icons.Outlined.Schedule,
					contentDescription = null,
					tint = status.color
				)
		}
		Text(
			text = status.text,
			style = MaterialTheme.typography.bodySmall,
			color = status.color,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
	}
}

@Composable
private fun SyntheticTermSubject.status(
	availableText: String,
	availableIcon: SubjectStatusIcon
): SubjectStatus {
	return when (availability) {
		SyntheticTermSubjectAvailability.AVAILABLE ->
			SubjectStatus(
				text = availableText,
				color = SuccessColor,
				icon = availableIcon
			)

		SyntheticTermSubjectAvailability.SELECTED ->
			SubjectStatus(
				text = stringResource(Res.string.create_term_subject_selected),
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				icon = SubjectStatusIcon.Dot
			)

		SyntheticTermSubjectAvailability.ALREADY_TAKEN ->
			SubjectStatus(
				text = stringResource(Res.string.create_term_subject_already_taken),
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				icon = SubjectStatusIcon.Dot
			)

		SyntheticTermSubjectAvailability.ALREADY_PLANNED ->
			SubjectStatus(
				text = stringResource(Res.string.create_term_subject_already_planned),
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				icon = SubjectStatusIcon.Dot
			)

		SyntheticTermSubjectAvailability.UNAVAILABLE ->
			SubjectStatus(
				text = stringResource(Res.string.create_term_subject_requirement_pending),
				color = WarningColor,
				icon = SubjectStatusIcon.Clock
			)
	}
}

private sealed class LoadChipStatus {
	data object Placeholder : LoadChipStatus()
	data object Loading : LoadChipStatus()
	data object Unavailable : LoadChipStatus()
	data class Available(val band: SyntheticTermLoadBand) : LoadChipStatus()
}

@Composable
private fun LoadChipStatus.label(): String {
	return when (this) {
		LoadChipStatus.Placeholder ->
			stringResource(Res.string.create_term_load_placeholder)
		LoadChipStatus.Loading ->
			stringResource(Res.string.create_term_load_loading)
		LoadChipStatus.Unavailable ->
			stringResource(Res.string.create_term_load_unavailable)
		is LoadChipStatus.Available ->
			stringResource(Res.string.create_term_load_prefix, band.label.lowercase())
	}
}

@Composable
private fun LoadChipStatus.color(): Color {
	return when (this) {
		LoadChipStatus.Placeholder ->
			MaterialTheme.colorScheme.outline

		LoadChipStatus.Loading ->
			MaterialTheme.colorScheme.primary

		LoadChipStatus.Unavailable ->
			MaterialTheme.colorScheme.onSurfaceVariant

		is LoadChipStatus.Available ->
			when (band) {
				SyntheticTermLoadBand.LIGHT ->
					LightLoadColor

				SyntheticTermLoadBand.MANAGEABLE ->
					ManageableLoadColor

				SyntheticTermLoadBand.NORMAL ->
					SuccessColor

				SyntheticTermLoadBand.DEMANDING ->
					WarningColor

				SyntheticTermLoadBand.VERY_DEMANDING ->
					MaterialTheme.colorScheme.error
			}
	}
}

private data class SubjectStatus(
	val text: String,
	val color: Color,
	val icon: SubjectStatusIcon
)

private enum class SubjectStatusIcon {
	Dot,
	Check,
	Clock
}

private enum class SubjectCardAction {
	Add,
	Remove
}

private val TermDropdownMaxHeight = 280.dp
private val LoadChipWidth = 176.dp
private val SuccessColor = Color(0xFF91EE9A)
private val LightLoadColor = Color(0xFF8CCBFF)
private val ManageableLoadColor = Color(0xFF7DE7C6)
private val WarningColor = Color(0xFFFFC400)
