package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.presentation.contract.Record
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
	RecordScreen(
		state = state,
		onRetryClick = onRetryClick,
		onSubjectGradeChange = onSubjectGradeChange,
		loadingContent = {
			RecordLoadingView()
		},
		contentStateContent = { contentState, subjectGradeChange ->
			RecordContentView(
				state = contentState,
				onSubjectGradeChange = subjectGradeChange
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
						imageVector = ImageVector.vectorResource(id = R.drawable.il_record_empty),
						contentDescription = null
					)
				}
			)
		}
	)
}
