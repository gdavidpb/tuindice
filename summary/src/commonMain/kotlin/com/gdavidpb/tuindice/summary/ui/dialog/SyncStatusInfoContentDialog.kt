package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.presentation.mapper.EnrollmentAnnulmentTexts
import com.gdavidpb.tuindice.base.presentation.mapper.NewStudentNoRecordTexts
import com.gdavidpb.tuindice.base.presentation.model.asString
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.dialog_button_close
import tuindice.summary.generated.resources.dialog_button_enter_password
import tuindice.summary.generated.resources.dialog_button_understood
import tuindice.summary.generated.resources.dialog_button_update_password
import tuindice.summary.generated.resources.dialog_message_not_enrolled
import tuindice.summary.generated.resources.dialog_message_record_access_denied
import tuindice.summary.generated.resources.dialog_message_sync_failed
import tuindice.summary.generated.resources.dialog_message_sync_missing_credentials
import tuindice.summary.generated.resources.dialog_message_sync_outdated_credentials
import tuindice.summary.generated.resources.dialog_message_sync_sources_enrollment_unavailable
import tuindice.summary.generated.resources.dialog_message_sync_sources_record_and_enrollment_unavailable
import tuindice.summary.generated.resources.dialog_message_sync_sources_record_unavailable
import tuindice.summary.generated.resources.dialog_message_sync_unavailable
import tuindice.summary.generated.resources.dialog_title_not_enrolled
import tuindice.summary.generated.resources.dialog_title_record_access_denied
import tuindice.summary.generated.resources.dialog_title_sync_failed
import tuindice.summary.generated.resources.dialog_title_sync_missing_credentials
import tuindice.summary.generated.resources.dialog_title_sync_outdated_credentials
import tuindice.summary.generated.resources.dialog_title_sync_sources_unavailable
import tuindice.summary.generated.resources.dialog_title_sync_unavailable

@Composable
fun SyncStatusInfoContentDialog(
	syncStatus: SyncStatus,
	syncReport: SyncReport = SyncReport.success(),
	hasCurrentTerm: Boolean = false,
	onUpdatePasswordClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	// A failed sync always reports its sources as unavailable, so that alone cannot pick the dialog:
	// the statuses that explain themselves (a new student, a denied record, a password to type) go
	// to their own copy below.
	if (!syncStatus.explainsItself && syncReport.hasUnavailableSource) {
		SyncStatusInfoDialog(
			titleText = stringResource(Res.string.dialog_title_sync_sources_unavailable),
			messageText = syncReport.unavailableSourcesMessage(),
			confirmText = stringResource(Res.string.dialog_button_understood),
			onConfirmClick = {},
			onDismissRequest = onDismissRequest
		)
		return
	}

	when (syncStatus) {
		SyncStatus.Healthy -> EnrollmentInfoDialog(
			syncReport = syncReport,
			hasCurrentTerm = hasCurrentTerm,
			onDismissRequest = onDismissRequest
		)

		SyncStatus.NewStudentNoRecord -> SyncStatusInfoDialog(
			titleText = NewStudentNoRecordTexts.title.asString(),
			messageText = NewStudentNoRecordTexts.message.asString(),
			confirmText = stringResource(Res.string.dialog_button_understood),
			onConfirmClick = {},
			onDismissRequest = onDismissRequest
		)

		SyncStatus.RecordAccessDenied -> SyncStatusInfoDialog(
			titleText = stringResource(Res.string.dialog_title_record_access_denied),
			messageText = stringResource(Res.string.dialog_message_record_access_denied),
			confirmText = stringResource(Res.string.dialog_button_understood),
			onConfirmClick = {},
			onDismissRequest = onDismissRequest
		)

		SyncStatus.Unavailable -> SyncStatusInfoDialog(
			titleText = stringResource(Res.string.dialog_title_sync_unavailable),
			messageText = stringResource(Res.string.dialog_message_sync_unavailable),
			confirmText = stringResource(Res.string.dialog_button_understood),
			onConfirmClick = {},
			onDismissRequest = onDismissRequest
		)

		SyncStatus.Failed -> SyncStatusInfoDialog(
			titleText = stringResource(Res.string.dialog_title_sync_failed),
			messageText = stringResource(Res.string.dialog_message_sync_failed),
			confirmText = stringResource(Res.string.dialog_button_understood),
			onConfirmClick = {},
			onDismissRequest = onDismissRequest
		)

		SyncStatus.OutdatedCredentials,
		SyncStatus.MissingCredentials -> PasswordRequiredDialog(
			syncStatus = syncStatus,
			onUpdatePasswordClick = onUpdatePasswordClick,
			onDismissRequest = onDismissRequest
		)
	}
}

private val SyncStatus.explainsItself: Boolean
	get() = requiresPassword ||
		this == SyncStatus.NewStudentNoRecord ||
		this == SyncStatus.RecordAccessDenied

// Both latches end the same way, with the user typing the password again.
@Composable
private fun PasswordRequiredDialog(
	syncStatus: SyncStatus,
	onUpdatePasswordClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val isOutdated = syncStatus == SyncStatus.OutdatedCredentials

	SyncStatusInfoDialog(
		titleText = stringResource(
			if (isOutdated) {
				Res.string.dialog_title_sync_outdated_credentials
			} else {
				Res.string.dialog_title_sync_missing_credentials
			}
		),
		messageText = stringResource(
			if (isOutdated) {
				Res.string.dialog_message_sync_outdated_credentials
			} else {
				Res.string.dialog_message_sync_missing_credentials
			}
		),
		confirmText = stringResource(
			if (isOutdated) Res.string.dialog_button_update_password else Res.string.dialog_button_enter_password
		),
		dismissText = stringResource(Res.string.dialog_button_close),
		onConfirmClick = onUpdatePasswordClick,
		onDismissRequest = onDismissRequest
	)
}

// What the university reports about the enrollment when the sync itself is healthy: an annulled
// enrollment (provisional while the record still has a current term, final once it does not) or no
// enrollment at all. An annulment names its cause, so it wins over the plain "not enrolled".
@Composable
private fun EnrollmentInfoDialog(
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

@Composable
private fun SyncReport.unavailableSourcesMessage(): String {
	val recordUnavailable = sources.record.status == SyncSourceStatus.Unavailable
	val enrollmentUnavailable = sources.enrollment.status == SyncSourceStatus.Unavailable

	return when {
		recordUnavailable && enrollmentUnavailable ->
			stringResource(Res.string.dialog_message_sync_sources_record_and_enrollment_unavailable)

		recordUnavailable ->
			stringResource(Res.string.dialog_message_sync_sources_record_unavailable)

		enrollmentUnavailable ->
			stringResource(Res.string.dialog_message_sync_sources_enrollment_unavailable)

		else ->
			stringResource(Res.string.dialog_message_sync_unavailable)
	}
}
