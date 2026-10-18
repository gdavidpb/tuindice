package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.runtime.snapshots.Snapshot
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantNavTarget
import com.gdavidpb.tuindice.base.presentation.navigation.DialogDestination
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination

// The chat's names of the screens and of the records, for the buttons the term report shows: the report's actions reach
// the screens through the same jumps the chat uses, so they need the names the chat's jumps take.
private val chatNavTargets: Map<AssistantNavTarget, NavTarget> = mapOf(
	AssistantNavTarget.SUMMARY to NavTarget.SUMMARY,
	AssistantNavTarget.STATISTICS to NavTarget.STATISTICS,
	AssistantNavTarget.SHARE_TERM to NavTarget.SHARE_TERM,
	AssistantNavTarget.RECORD to NavTarget.RECORD,
	AssistantNavTarget.RECORD_PROJECTION to NavTarget.RECORD_PROJECTION,
	AssistantNavTarget.RECORD_SCHEDULE to NavTarget.RECORD_SCHEDULE,
	AssistantNavTarget.RECORD_CREATE_TERM to NavTarget.RECORD_CREATE_TERM,
	AssistantNavTarget.EVALUATIONS to NavTarget.EVALUATIONS,
	AssistantNavTarget.EVALUATIONS_ADD to NavTarget.EVALUATIONS_ADD,
	AssistantNavTarget.EVALUATIONS_DETAIL to NavTarget.EVALUATIONS_DETAIL,
	AssistantNavTarget.PENSUM to NavTarget.PENSUM,
	AssistantNavTarget.SUBJECTS_SEARCH to NavTarget.SUBJECTS_SEARCH,
	AssistantNavTarget.SUBJECTS_DETAIL to NavTarget.SUBJECTS_DETAIL,
	AssistantNavTarget.ENROLLMENT_PROOF to NavTarget.ENROLLMENT_PROOF,
	AssistantNavTarget.ABOUT to NavTarget.ABOUT,
	AssistantNavTarget.SIGN_IN to NavTarget.SIGN_IN,
	AssistantNavTarget.UPDATE_PASSWORD to NavTarget.UPDATE_PASSWORD
)

internal fun AssistantNavTarget.toChatNavTarget(): NavTarget {
	return chatNavTargets.getValue(this)
}

internal fun AssistantEntityRef.toChatEntityRef(): EntityRef {
	return EntityRef(
		kind = when (kind) {
			AssistantEntityKind.TERM -> EntityKind.TERM
			AssistantEntityKind.SUBJECT -> EntityKind.SUBJECT
			AssistantEntityKind.EVALUATION -> EntityKind.EVALUATION
			AssistantEntityKind.ATTEMPT -> EntityKind.ATTEMPT
			AssistantEntityKind.PENSUM -> EntityKind.PENSUM
			AssistantEntityKind.PLAN -> EntityKind.PLAN
		},
		id = id
	)
}

// A tap on the push of a term report opens the report over the summary tab, so its back returns to the summary. The
// dialogs on top of the tab the student is on, the assistant's chat among them, are closed in the same change, so the
// report is not left under a sheet of another tab. Nothing happens before the student is signed in: the host waits for
// that before it asks.
internal fun TuIndiceNavigator.showTermReport(insightId: String) {
	if (rootMode != TuIndiceRootMode.SIGNED_IN) return

	Snapshot.withMutableSnapshot {
		closeDialogsOnTop()
		showOverTab(section = MainSection.SUMMARY, key = SummaryDestination.TermReport(insightId = insightId))
	}
}

// Takes off the dialogs on top of the tab the student is on, the chat among them. The entries under them stay. The
// current key is a dialog only when the stack holds more than its root, so the pop never switches the tab.
private fun TuIndiceNavigator.closeDialogsOnTop() {
	while (currentKey is DialogDestination) {
		if (!pop()) break
	}
}
