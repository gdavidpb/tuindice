package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailability
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.CreateTermAddSubjectTab
import com.gdavidpb.tuindice.record.ui.model.CreateTermSubjectCardAction
import com.gdavidpb.tuindice.record.ui.view.AlreadyTakenSearchResultsToggle
import com.gdavidpb.tuindice.record.ui.view.CreateTermAddSubjectTabs
import com.gdavidpb.tuindice.record.ui.view.CreateTermPeriodRow
import com.gdavidpb.tuindice.record.ui.view.CreateTermSearchField
import com.gdavidpb.tuindice.record.ui.view.CreateTermSectionTitle
import com.gdavidpb.tuindice.record.ui.view.CreateTermSelectedSubjectCard
import com.gdavidpb.tuindice.record.ui.view.CreateTermSubmitBar
import com.gdavidpb.tuindice.record.ui.view.CreateTermSuggestedSubjectCard
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_add_subjects_title
import tuindice.record.generated.resources.create_term_no_suggestions
import tuindice.record.generated.resources.create_term_search_error
import tuindice.record.generated.resources.create_term_search_results
import tuindice.record.generated.resources.create_term_selected_title
import tuindice.record.generated.resources.create_term_suggested_title

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
	val selectedAddSubjectTab = remember { mutableStateOf(CreateTermAddSubjectTab.Suggested) }
	val showTakenSearchResults = remember { mutableStateOf(false) }
	val selectedSubjectCodes = remember(state.selectedSubjects) {
		state.selectedSubjects.map(SyntheticTermSubject::subjectCode).toSet()
	}
	val displayedSuggestedSubjects = state.suggestedSubjects.filterNot { subject ->
		subject.subjectCode in selectedSubjectCodes
	}
	val searchResultsWithoutSelectedSubjects = state.searchResults.filterNot { subject ->
		subject.subjectCode in selectedSubjectCodes
	}
	val isSearchQueryReady = state.query.trim().length >= MinimumSearchQueryLength

	LaunchedEffect(state.query) {
		showTakenSearchResults.value = false
	}

	LaunchedEffect(selectedAddSubjectTab.value) {
		if (selectedAddSubjectTab.value == CreateTermAddSubjectTab.Search) {
			focusRequester.requestFocus()
		}
	}

	val takenSearchResultsCount = searchResultsWithoutSelectedSubjects.count { subject ->
		subject.availability == SyntheticTermSubjectAvailability.ALREADY_TAKEN
	}
	val displayedSearchResults = if (showTakenSearchResults.value) {
		searchResultsWithoutSelectedSubjects
	} else {
		searchResultsWithoutSelectedSubjects.filterNot { subject ->
			subject.availability == SyntheticTermSubjectAvailability.ALREADY_TAKEN
		}
	}

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

			if (state.selectedSubjects.isNotEmpty()) {
				item {
					CreateTermSectionTitle(text = stringResource(Res.string.create_term_selected_title))
				}
				items(
					items = state.selectedSubjects,
					key = { subject -> SelectedSubjectKeyPrefix + subject.subjectCode }
				) { subject ->
					CreateTermSelectedSubjectCard(
						modifier = Modifier.animateItem(
							fadeInSpec = null,
							fadeOutSpec = null
						),
						subject = subject,
						action = CreateTermSubjectCardAction.Remove,
						onClick = { onSubjectRemove(subject.subjectCode) }
					)
				}
			}

			item {
				CreateTermSectionTitle(text = stringResource(Res.string.create_term_add_subjects_title))
			}

			item {
				CreateTermAddSubjectTabs(
					selectedTab = selectedAddSubjectTab.value,
					onTabSelected = { tab -> selectedAddSubjectTab.value = tab }
				)
			}

			when (selectedAddSubjectTab.value) {
				CreateTermAddSubjectTab.Suggested -> {
					item {
						CreateTermSectionTitle(text = stringResource(Res.string.create_term_suggested_title))
					}
					if (displayedSuggestedSubjects.isEmpty()) {
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
									items = displayedSuggestedSubjects,
									key = { subject -> SuggestedSubjectKeyPrefix + subject.subjectCode }
								) { subject ->
									CreateTermSuggestedSubjectCard(
										modifier = Modifier.animateItem(
											fadeInSpec = null,
											fadeOutSpec = null
										),
										subject = subject,
										enabled = subject.canAdd,
										onClick = { onSubjectAdd(subject) }
									)
								}
							}
						}
					}
				}

				CreateTermAddSubjectTab.Search -> {
					item {
						CreateTermSearchField(
							query = state.query,
							focusRequester = focusRequester,
							onQueryChange = onQueryChange,
							onClearQueryClick = onClearQueryClick
						)
					}

					if (isSearchQueryReady) {
						item {
							CreateTermSectionTitle(
								text = stringResource(
									Res.string.create_term_search_results,
									displayedSearchResults.size
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
							items = displayedSearchResults,
							key = { subject -> SearchResultSubjectKeyPrefix + subject.subjectCode }
						) { subject ->
							CreateTermSelectedSubjectCard(
								modifier = Modifier.animateItem(
									fadeInSpec = null,
									fadeOutSpec = null
								),
								subject = subject,
								action = CreateTermSubjectCardAction.Add,
								enabled = subject.canAdd,
								onClick = { onSubjectAdd(subject) }
							)
						}
					}
				}
			}

			if (
				selectedAddSubjectTab.value == CreateTermAddSubjectTab.Search &&
				isSearchQueryReady &&
				takenSearchResultsCount > 0
			) {
				item {
					AlreadyTakenSearchResultsToggle(
						count = takenSearchResultsCount,
						isExpanded = showTakenSearchResults.value,
						onClick = {
							showTakenSearchResults.value = !showTakenSearchResults.value
						}
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

private const val MinimumSearchQueryLength = 2
private const val SearchResultSubjectKeyPrefix = "search:"
private const val SuggestedSubjectKeyPrefix = "suggested:"
private const val SelectedSubjectKeyPrefix = "selected:"
