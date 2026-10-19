package com.gdavidpb.tuindice.assistant.presentation.machine.collaborator

import com.gdavidpb.tuindice.base.domain.repository.NotificationOfferRepository
import com.gdavidpb.tuindice.base.domain.repository.NotificationPermissionRepository

// The offer of the notification permission in the empty chat: whether the platform still asks for it, and the mark that
// the offer was made on this device. The effect that asks for the permission is the route's, through [permission].
class AssistantChatNotificationOffer(
	private val permission: NotificationPermissionRepository,
	private val offers: NotificationOfferRepository
) {
	suspend fun isOfferNeeded(): Boolean = permission.isOfferNeeded()

	fun markOffered() = offers.setOffered(true)
}
