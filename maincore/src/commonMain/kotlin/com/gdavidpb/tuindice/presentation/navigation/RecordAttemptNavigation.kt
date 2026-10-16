package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.repository.RecordAttemptFocusRepository

/**
 * The jump to an attempt of Registro, from Summary: leaves the request for the tab to apply when it is
 * ready, then closes whatever entry is on top and goes there in one change of the stack. The request is
 * state, so it holds until the tab consumes it, and the start tab stays as it was.
 */
fun TuIndiceNavigator.showRecordAttempt(
	attemptFocus: RecordAttemptFocusRepository,
	attemptId: String
) {
	attemptFocus.request(attemptId = attemptId)
	popThenSwitchTab(MainSection.RECORD)
}
