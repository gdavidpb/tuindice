package com.gdavidpb.tuindice.base.domain.repository

/**
 * The permission to show notifications, as the platform gives it. The chat offers it once to the student, and asks
 * for it only on the student's word.
 */
interface NotificationPermissionRepository {
	/**
	 * Whether the chat should offer the permission now: the platform still asks for it on this device (not granted, and
	 * prompted by the system), and the offer was not made on this device yet.
	 */
	suspend fun isOfferNeeded(): Boolean

	/** Opens the system's request for the permission. Its answer is not awaited: the offer has already been made. */
	fun requestPermission()
}
