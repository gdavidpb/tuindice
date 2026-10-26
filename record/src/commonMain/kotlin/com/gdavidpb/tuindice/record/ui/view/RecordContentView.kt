package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.ReportComposition
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.record.domain.model.filteredProjectionFor
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.mapper.RecordMapperTexts
import com.gdavidpb.tuindice.record.presentation.mapper.takeIfAbovePager
import com.gdavidpb.tuindice.record.presentation.mapper.toTermItemList
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.dialog.TermSelectionBottomSheet
import com.gdavidpb.tuindice.record.ui.model.RecordActions
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.term_attempt_credits_pattern
import tuindice.record.generated.resources.term_attempt_grade_pattern
import tuindice.record.generated.resources.term_attempt_section_classroom_pattern
import tuindice.record.generated.resources.term_attempt_section_pattern
import tuindice.record.generated.resources.term_credits_pattern
import tuindice.record.generated.resources.term_grade_diff_pattern
import tuindice.record.generated.resources.term_grade_sum_pattern

@Composable
fun RecordContentView(
	state: Record.State.Content,
	selectedTermId: String?,
	actions: RecordActions,
	showTermSelection: Boolean,
	onScrollInProgressChange: (Boolean) -> Unit = {}
) {
	ReportComposition("record-content")

	val texts = rememberRecordMapperTexts()
	// The projection is redone only for what it projects: the record, the gesture's override and the view mode.
	val projection = remember(state.record, state.inFlightSelection, state.viewMode) {
		state.visibleRecord.filteredProjectionFor(state.viewMode)
	}
	val terms = remember(projection, texts, state.viewMode, state.notice, state.arrivalAttemptId) {
		projection.terms
			.toTermItemList(
				viewMode = state.viewMode,
				texts = texts,
				notice = state.notice,
				arrivalAttemptId = state.arrivalAttemptId
			)
			.asReversed()
	}
	val effectiveSelectedTermId = selectedTermId ?: terms.firstOrNull()?.termId

	Column(
		modifier = Modifier
			.fillMaxSize()
			.testTag(RecordUiTags.ContentContainer)
	) {
		if (terms.isNotEmpty() && (effectiveSelectedTermId != null)) {
			RecordTermPagerView(
				modifier = Modifier
					.fillMaxSize()
					.padding(top = InternalScreenDefaults.TopBarSpacing),
				terms = terms,
				texts = texts,
				notice = state.notice?.takeIfAbovePager(),
				selectedTermId = effectiveSelectedTermId,
				actions = actions,
				onScrollInProgressChange = onScrollInProgressChange
			)
		}
	}

	if (showTermSelection && terms.isNotEmpty() && effectiveSelectedTermId != null) {
		TermSelectionBottomSheet(
			terms = terms,
			selectedTermId = effectiveSelectedTermId,
			viewMode = state.viewMode,
			texts = texts,
			actions = actions
		)
	}
}

// The texts the record's numbers are written with, read from the app's strings. Every view that shows a number builds
// its text with these, and they are kept while the strings stay the same.
@Composable
internal fun rememberRecordMapperTexts(): RecordMapperTexts {
	val termGradeDiffPattern = stringResource(Res.string.term_grade_diff_pattern)
	val termGradeSumPattern = stringResource(Res.string.term_grade_sum_pattern)
	val termCreditsPattern = stringResource(Res.string.term_credits_pattern)
	val attemptGradePattern = stringResource(Res.string.term_attempt_grade_pattern)
	val attemptCreditsPattern = stringResource(Res.string.term_attempt_credits_pattern)
	val attemptSectionPattern = stringResource(Res.string.term_attempt_section_pattern)
	val attemptSectionClassroomPattern = stringResource(Res.string.term_attempt_section_classroom_pattern)

	return remember(
		termGradeDiffPattern,
		termGradeSumPattern,
		termCreditsPattern,
		attemptGradePattern,
		attemptCreditsPattern,
		attemptSectionPattern,
		attemptSectionClassroomPattern
	) {
		RecordMapperTexts(
			termGrade = { grade ->
				termGradeDiffPattern.replace("%1${'$'}.4f", grade.formatGrade(decimals = 4))
			},
			termGradeSum = { grade ->
				termGradeSumPattern.replace("%1${'$'}.4f", grade.formatGrade(decimals = 4))
			},
			termCredits = { credits ->
				termCreditsPattern.replace("%1${'$'}d", credits.toString())
			},
			termAttemptGrade = { grade ->
				attemptGradePattern.replace("%1${'$'}d", grade.toString())
			},
			termAttemptCredits = { credits ->
				attemptCreditsPattern.replace("%1${'$'}d", credits.toString())
			},
			termAttemptSection = { section ->
				attemptSectionPattern.replace("%1${'$'}d", section.toString())
			},
			termAttemptSectionClassroom = { section, classroom ->
				attemptSectionClassroomPattern
					.replace("%1${'$'}d", section.toString())
					.replace("%2${'$'}s", classroom)
			}
		)
	}
}
