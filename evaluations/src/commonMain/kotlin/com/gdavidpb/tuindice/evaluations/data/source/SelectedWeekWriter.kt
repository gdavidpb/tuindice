package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.evaluations.domain.model.SelectedWeekPick
import com.gdavidpb.tuindice.evaluations.utils.PreferencesKeys
import com.gdavidpb.tuindice.evaluations.utils.SelectedWeekOrder
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// The one holder of the selected week, shared by every machine: it writes the stored week, mirrors it for the
// screens and lets it go when the session ends. Saves run one at a time. A pick is written only if it is newer than
// the last pick written and than the floor of the session, so the week left stored is the last pick made, whatever
// the order the saves run in. The end of a session raises the floor, so the picks of the session that ends are not
// written after it, and it removes the stored week, so a save that lands between the two releases of a wipe does
// not survive the second one.
class SelectedWeekWriter(
	private val settings: Settings
) : SessionMemory {
	private val turn = Mutex()

	// The order of the last pick written, and the order from which the picks of the session are kept.
	private var writtenOrder = 0L
	private var sessionOrder = 0L

	// The mirror of the stored week, which the screen reads.
	private val selectedWeekKey = MutableStateFlow(
		settings.getStringOrNull(PreferencesKeys.SELECTED_EVALUATIONS_WEEK)
	)

	fun observeSelectedWeekKey(): Flow<String?> {
		return selectedWeekKey
	}

	suspend fun save(pick: SelectedWeekPick) {
		turn.withLock {
			if (pick.order > maxOf(writtenOrder, sessionOrder)) {
				settings.putString(PreferencesKeys.SELECTED_EVALUATIONS_WEEK, pick.weekKey)
				selectedWeekKey.value = pick.weekKey
				writtenOrder = pick.order
			}
		}
	}

	// The end of a session releases the memory before the wipe and again after it. Each release removes the stored
	// week, so the second one removes what a save wrote in between, and raises the floor, so that the picks made
	// before it are not written after it. The mirror follows the stored week, which is empty from then on.
	override suspend fun clearSessionMemory() {
		turn.withLock {
			sessionOrder = SelectedWeekOrder.next()
			settings.remove(PreferencesKeys.SELECTED_EVALUATIONS_WEEK)
			selectedWeekKey.value = null
		}
	}
}
