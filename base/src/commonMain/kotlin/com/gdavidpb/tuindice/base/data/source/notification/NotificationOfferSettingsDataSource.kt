package com.gdavidpb.tuindice.base.data.source.notification

import com.gdavidpb.tuindice.base.domain.repository.NotificationOfferRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow

// The offer is made once per student on the device: the mark is written when the offer shows, and the wipe at the end
// of the session takes it with the rest of the stored data.
class NotificationOfferSettingsDataSource(
	private val settings: Settings
) : NotificationOfferRepository, SessionMemory {
	private val offered = MutableStateFlow(readStoredOffer())

	override fun isOffered(): Boolean = offered.value

	override fun setOffered(offered: Boolean) {
		settings.putBoolean(OFFERED_KEY, offered)
		this.offered.value = offered
	}

	// The mirror goes back to what is stored, which the wipe has just emptied.
	override suspend fun clearSessionMemory() {
		offered.value = readStoredOffer()
	}

	private fun readStoredOffer(): Boolean {
		return settings.getBooleanOrNull(OFFERED_KEY) ?: false
	}

	private companion object {
		const val OFFERED_KEY = "notificationPermissionOffered"
	}
}
