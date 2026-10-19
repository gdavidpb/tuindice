package com.gdavidpb.tuindice.assistant.presentation.testing

import com.gdavidpb.tuindice.base.domain.repository.NotificationOfferRepository
import com.gdavidpb.tuindice.base.domain.repository.NotificationPermissionRepository

// The platform's permission: it still asks while [platformAsks], and the offer is not needed once the device has made
// it. Like the real ones, it answers "needed" only when both hold.
internal class RecordingNotificationPermissionRepository(
	private val offers: NotificationOfferRepository
) : NotificationPermissionRepository {
	var platformAsks = true
	var requests = 0

	override suspend fun isOfferNeeded(): Boolean = platformAsks && !offers.isOffered()

	override fun requestPermission() {
		requests++
	}
}

// The mark of the offer on the device, counted, so a test can tell how many times the offer was made.
internal class RecordingNotificationOfferRepository : NotificationOfferRepository {
	private var mark = false
	var marks = 0

	override fun isOffered(): Boolean = mark

	override fun setOffered(offered: Boolean) {
		mark = offered
		if (offered) marks++
	}
}
