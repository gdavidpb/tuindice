package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination

/**
 * The form that plans a term, from the invitation of Summary: opened over the Record tab in one move, the same form
 * Record's own button opens. Like the other jumps of the hub it does not write the section the app opens on.
 */
fun TuIndiceNavigator.showCreateTerm() {
	showOverTab(
		section = MainSection.RECORD,
		key = RecordDestination.CreateSyntheticTerm()
	)
}
