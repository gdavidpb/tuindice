package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.runtime.snapshots.Snapshot
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.presentation.navigation.Destination

/**
 * Goes to [section] with [key] opened over its stack, in one change of the observable stack: no frame shows the tab
 * without the entry. Like [TuIndiceNavigator.switchTab] it leaves the start tab alone, so back from [key] returns to
 * where the app started, and an entry already on top of that stack is not pushed twice. Nothing happens outside a
 * signed-in session.
 */
fun TuIndiceNavigator.showOverTab(section: MainSection, key: Destination) {
	if (rootMode != TuIndiceRootMode.SIGNED_IN) return

	Snapshot.withMutableSnapshot {
		switchTab(section)
		push(key)
	}
}
