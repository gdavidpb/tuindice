package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.evaluations.domain.model.SelectedWeekPick
import com.gdavidpb.tuindice.evaluations.testing.FakeSettings
import com.gdavidpb.tuindice.evaluations.utils.PreferencesKeys
import com.gdavidpb.tuindice.evaluations.utils.SelectedWeekOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SelectedWeekWriterTest {
	private val settings = FakeSettings()
	private val writer = SelectedWeekWriter(settings = settings)

	@Test
	fun save_anOlderPickLandingAfterANewerOne_keepsTheNewerWeek() = runTest {
		val olderPick = pickOf(WEEK_OF_THE_FIRST_PICK)
		val newerPick = pickOf(WEEK_OF_THE_SECOND_PICK)

		writer.save(pick = newerPick)
		writer.save(pick = olderPick)

		assertEquals(WEEK_OF_THE_SECOND_PICK, settings.getStringOrNull(PreferencesKeys.SELECTED_EVALUATIONS_WEEK))
		assertEquals(WEEK_OF_THE_SECOND_PICK, writer.observeSelectedWeekKey().first())
	}

	@Test
	fun save_aPickMadeBeforeTheSessionEnded_isNotWritten() = runTest {
		val pick = pickOf(WEEK_OF_THE_FIRST_PICK)

		writer.clearSessionMemory()
		writer.save(pick = pick)

		assertNull(settings.getStringOrNull(PreferencesKeys.SELECTED_EVALUATIONS_WEEK))
		assertNull(writer.observeSelectedWeekKey().first())
	}

	@Test
	fun save_aPickMadeAfterTheSessionEnded_isWritten() = runTest {
		writer.clearSessionMemory()
		val pick = pickOf(WEEK_OF_THE_FIRST_PICK)

		writer.save(pick = pick)

		assertEquals(WEEK_OF_THE_FIRST_PICK, settings.getStringOrNull(PreferencesKeys.SELECTED_EVALUATIONS_WEEK))
		assertEquals(WEEK_OF_THE_FIRST_PICK, writer.observeSelectedWeekKey().first())
	}

	// The leak: the first release runs before the wipe, and a pick made after it is saved between the wipe and the
	// second release. The second release removes that week, so the next account does not open on it.
	@Test
	fun clearSessionMemory_aPickSavedBetweenTheWipeAndTheSecondRelease_isRemovedByTheSecondRelease() = runTest {
		writer.clearSessionMemory()
		settings.clear()
		val pick = pickOf(WEEK_OF_THE_FIRST_PICK)
		writer.save(pick = pick)

		writer.clearSessionMemory()

		assertNull(settings.getStringOrNull(PreferencesKeys.SELECTED_EVALUATIONS_WEEK))
		assertNull(writer.observeSelectedWeekKey().first())
	}

	@Test
	fun observeSelectedWeekKey_afterEveryOperation_matchesTheStoredWeek() = runTest {
		val olderPick = pickOf(WEEK_OF_THE_FIRST_PICK)
		val newerPick = pickOf(WEEK_OF_THE_SECOND_PICK)
		assertMirrorMatchesSettings()

		writer.save(pick = newerPick)
		assertMirrorMatchesSettings()

		writer.save(pick = olderPick)
		assertMirrorMatchesSettings()

		writer.clearSessionMemory()
		assertMirrorMatchesSettings()

		writer.save(pick = pickOf(WEEK_OF_THE_FIRST_PICK))
		assertMirrorMatchesSettings()

		writer.clearSessionMemory()
		assertMirrorMatchesSettings()
	}

	@Test
	fun observeSelectedWeekKey_ofAWriterOverAStoredWeek_startsWithThatWeek() = runTest {
		val storedSettings = FakeSettings().apply {
			putString(PreferencesKeys.SELECTED_EVALUATIONS_WEEK, WEEK_OF_THE_FIRST_PICK)
		}

		val storedWriter = SelectedWeekWriter(settings = storedSettings)

		assertEquals(WEEK_OF_THE_FIRST_PICK, storedWriter.observeSelectedWeekKey().first())
	}

	private suspend fun assertMirrorMatchesSettings() {
		assertEquals(
			settings.getStringOrNull(PreferencesKeys.SELECTED_EVALUATIONS_WEEK),
			writer.observeSelectedWeekKey().first()
		)
	}

	private fun pickOf(weekKey: String) = SelectedWeekPick(order = SelectedWeekOrder.next(), weekKey = weekKey)

	private companion object {
		const val WEEK_OF_THE_FIRST_PICK = "academic:2026-09-28:5"
		const val WEEK_OF_THE_SECOND_PICK = "academic:2026-09-28:7"
	}
}
