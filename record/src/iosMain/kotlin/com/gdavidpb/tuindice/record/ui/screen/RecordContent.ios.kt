package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.mapper.RecordMapperTexts
import com.gdavidpb.tuindice.record.ui.view.RecordContentView
import com.gdavidpb.tuindice.record.ui.view.RecordEmptyView
import com.gdavidpb.tuindice.record.ui.view.RecordFailedView
import com.gdavidpb.tuindice.record.ui.view.RecordLoadingView
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.*

@Composable
fun RecordContentScreen(
	state: Record.State,
	onRetryClick: () -> Unit,
	onSubjectGradeChange: (
		quarterId: String,
		subjectId: String,
		newGrade: Int,
		isSelected: Boolean
	) -> Unit
) {
	val quarterGradeDiffPattern = stringResource(Res.string.quarter_grade_diff_pattern)
	val quarterGradeSumPattern = stringResource(Res.string.quarter_grade_sum_pattern)
	val quarterCreditsPattern = stringResource(Res.string.quarter_credits_pattern)
	val subjectStatusPattern = stringResource(Res.string.subject_status_pattern)
	val subjectGradePattern = stringResource(Res.string.subject_grade_pattern)
	val subjectCreditsPattern = stringResource(Res.string.subject_credits_pattern)
	val subjectRetiredText = stringResource(Res.string.subject_retired)

	val recordMapperTexts = remember {
		RecordMapperTexts(
			quarterGradeDiff = { grade ->
				quarterGradeDiffPattern.replace("%1${'$'}.4f", grade.formatGrade(decimals = 4))
			},
			quarterGradeSum = { grade ->
				quarterGradeSumPattern.replace("%1${'$'}.4f", grade.formatGrade(decimals = 4))
			},
			quarterCredits = { credits ->
				quarterCreditsPattern.replace("%1${'$'}d", credits.toString())
			},
			subjectRetired = subjectRetiredText,
			subjectStatus = { status ->
				subjectStatusPattern.replace("%1${'$'}s", status)
			},
			subjectGrade = { grade ->
				subjectGradePattern.replace("%1${'$'}d", grade.toString())
			},
			subjectCredits = { credits ->
				subjectCreditsPattern.replace("%1${'$'}d", credits.toString())
			}
		)
	}

	RecordScreen(
		state = state,
		onRetryClick = onRetryClick,
		onSubjectGradeChange = onSubjectGradeChange,
		loadingContent = {
			RecordLoadingView()
		},
		contentStateContent = { contentState, gradeChange ->
			RecordContentView(
				state = contentState,
				texts = recordMapperTexts,
				highlightColor = MaterialTheme.colorScheme.primary,
				onSubjectGradeChange = gradeChange
			)
		},
		failedContent = { retry ->
			RecordFailedView(
				title = stringResource(Res.string.record_failed_title),
				message = stringResource(Res.string.record_failed_message),
				retryText = stringResource(Res.string.record_failed_retry),
				onRetryClick = retry,
				headerContent = {
					ErrorStateAnimationView()
				}
			)
		},
		emptyContent = {
			RecordEmptyView(
				message = stringResource(Res.string.record_empty_illustration_message),
				highlightedParts = listOf(
					stringResource(Res.string.record_app_name),
					stringResource(Res.string.record_app_uni)
				),
				illustrationContent = {
					Image(
						painter = rememberVectorPainter(Icons.Outlined.Info),
						contentDescription = null,
						colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
					)
				}
			)
		}
	)
}
