package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.presentation.mapper.EnrollmentAnnulmentTexts
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.dialog_button_understood
import tuindice.summary.generated.resources.dialog_message_not_enrolled
import tuindice.summary.generated.resources.dialog_title_not_enrolled

// What the university reports about the enrollment when the sync itself is healthy: an annulled
// enrollment (provisional while the record still has a current term, final once it does not) or no
// enrollment at all. An annulment names its cause, so it wins over the plain "not enrolled".
@Composable
fun EnrollmentInfoDialog(
	syncReport: SyncReport,
	hasCurrentTerm: Boolean,
	onDismissRequest: () -> Unit
) {
	val enrollment = syncReport.sources.enrollment
	val situation = enrollment.situation

	when {
		situation != null -> SyncStatusInfoDialog(
			titleText = EnrollmentAnnulmentTexts.title(isProvisional = hasCurrentTerm).asString(),
			messageText = EnrollmentAnnulmentTexts.message(
				cause = situation.annulmentCause,
				isProvisional = hasCurrentTerm
			).asString(),
			confirmText = stringResource(Res.string.dialog_button_understood),
			onConfirmClick = {},
			onDismissRequest = onDismissRequest
		)

		enrollment.status == SyncSourceStatus.NotEnrolled -> SyncStatusInfoDialog(
			titleText = stringResource(Res.string.dialog_title_not_enrolled),
			messageText = stringResource(Res.string.dialog_message_not_enrolled),
			confirmText = stringResource(Res.string.dialog_button_understood),
			onConfirmClick = {},
			onDismissRequest = onDismissRequest
		)
	}
}
