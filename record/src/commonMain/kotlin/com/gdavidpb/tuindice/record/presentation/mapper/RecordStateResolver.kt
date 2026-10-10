package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.NewStudentNoRecordTexts
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.model.RecordFailedArt
import com.gdavidpb.tuindice.record.presentation.model.RecordNotice
import com.gdavidpb.tuindice.record.presentation.model.RecordNoticeKind
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.record_empty_message
import tuindice.record.generated.resources.record_empty_title
import tuindice.record.generated.resources.record_failed_message
import tuindice.record.generated.resources.record_failed_title

// What Failed says and the art above it. When the university has no record for the account yet
// nothing of ours failed, so the screen says that instead and leaves the error art out.
internal fun resolveRecordFailed(isNewStudentNoRecord: Boolean): Record.State.Failed {
	return if (isNewStudentNoRecord) {
		Record.State.Failed(
			title = NewStudentNoRecordTexts.title,
			message = NewStudentNoRecordTexts.message,
			art = RecordFailedArt.NoRecord
		)
	} else {
		Record.State.Failed(
			title = UiText.Resource(Res.string.record_failed_title),
			message = UiText.Resource(Res.string.record_failed_message),
			art = RecordFailedArt.Error
		)
	}
}

// What Empty says. A final annulment is why there is nothing to show: it replaces the generic copy.
internal fun resolveRecordEmpty(notice: RecordNotice?): Record.State.Empty {
	val annulment = notice?.takeIf { candidate -> candidate.kind == RecordNoticeKind.AnnulledFinal }

	return Record.State.Empty(
		title = annulment?.title ?: UiText.Resource(Res.string.record_empty_title),
		message = annulment?.message ?: UiText.Resource(Res.string.record_empty_message)
	)
}
