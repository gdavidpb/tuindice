package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.runtime.snapshots.Snapshot
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.presentation.navigation.DialogDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination

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
