package com.gdavidpb.tuindice.presentation.machine

import com.gdavidpb.tuindice.presentation.model.PendingSessionEnd
import com.gdavidpb.tuindice.presentation.model.SessionNoticeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

// What the interface owes the user when the session ended behind their back, decided by the machine from
// the notice (and its stamp) it is given: the way to the sign-in always, the notice only the first time
// that very notice is put in front of the interface.
class SessionEndTrackerTest {
	private val first = SessionNoticeText(stamp = 11, message = "first notice")
	private val later = SessionNoticeText(stamp = 22, message = "later notice")

	@Test
	fun aSignalWithoutANotice_isAPendingEndWithNothingToTell() {
		val tracker = SessionEndTracker()

		val pending = tracker.begin(current = null, notice = null)

		assertNull(pending.message)
	}

	@Test
	fun aNoticeThatWasAlreadyOffered_isNotOfferedAgainByTheSignalOfTheSameInvalidation() {
		val tracker = SessionEndTracker()
		val offeredByTheStartUp = tracker.begin(current = null, notice = first)
		assertEquals("first notice", offeredByTheStartUp.message)

		// The interface attended it, and the live signal of that same invalidation reads the same notice.
		val live = tracker.begin(current = null, notice = first)

		assertNull(live.message, "same stamp: navigate only")
		assertNotEquals(offeredByTheStartUp.id, live.id, "and it is a new end to attend")
	}

	@Test
	fun aLaterInvalidation_withANewStamp_carriesItsNotice() {
		val tracker = SessionEndTracker()
		tracker.begin(current = null, notice = first)

		val laterEnd = tracker.begin(current = null, notice = later)

		assertEquals("later notice", laterEnd.message)
	}

	@Test
	fun aSignalThatArrivesWhileAnEndIsPending_keepsItsNotice_neitherLostNorDuplicated() {
		val tracker = SessionEndTracker()
		val pending = tracker.begin(current = null, notice = first)

		assertEquals(pending, tracker.begin(current = pending, notice = first))
		assertEquals(pending, tracker.begin(current = pending, notice = null))
		assertEquals(pending, tracker.begin(current = pending, notice = later))
	}

	// The end that is pending had nothing to tell and the signal brings a notice: it is not lost, and
	// the route that is attending the old one starts again with the message (a new id).
	@Test
	fun aPendingEndWithNothingToTell_takesTheNoticeThatArrivesAfterwards() {
		val tracker = SessionEndTracker()
		val pending = tracker.begin(current = null, notice = null)

		val upgraded = tracker.begin(current = pending, notice = first)

		assertEquals("first notice", upgraded.message)
		assertNotEquals(pending.id, upgraded.id)
	}

	@Test
	fun aPendingEndWithNothingToTell_staysAsItIsWhenTheNoticeWasAlreadyOffered() {
		val tracker = SessionEndTracker()
		tracker.begin(current = null, notice = first)
		val pending = PendingSessionEnd(id = 99, message = null)

		assertEquals(pending, tracker.begin(current = pending, notice = first))
	}

	@Test
	fun attendingAnEnd_givesBackTheStampOfItsNoticeOnce_andNothingWhenItHadNone() {
		val tracker = SessionEndTracker()
		val withNotice = tracker.begin(current = null, notice = first)
		val withoutNotice = PendingSessionEnd(id = 99, message = null)

		assertNull(tracker.finish(withoutNotice))
		assertEquals(first.stamp, tracker.finish(withNotice))
		assertNull(tracker.finish(withNotice), "the notice is taken once")
	}
}
