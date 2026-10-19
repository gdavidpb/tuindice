package com.gdavidpb.tuindice.base.domain.repository

/**
 * Whether the notification permission was already offered to the student on this device. It is kept with the rest of
 * the stored data, so a sign-out makes the offer possible again for the next student.
 */
interface NotificationOfferRepository {
	fun isOffered(): Boolean

	// Writes the mark: true once the offer was made, false when a debug launch asks for the offer again.
	fun setOffered(offered: Boolean)
}
