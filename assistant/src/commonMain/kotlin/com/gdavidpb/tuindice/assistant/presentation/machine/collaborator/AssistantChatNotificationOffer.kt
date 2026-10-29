package com.gdavidpb.tuindice.assistant.presentation.machine.collaborator

import com.gdavidpb.tuindice.base.domain.repository.NotificationOfferRepository
import com.gdavidpb.tuindice.base.domain.repository.NotificationPermissionRepository

// The offer of the notification permission in the empty chat: whether the platform still asks for the permission, and
// the mark that the offer was made on this device. The request to the platform is not made here: the machine sends it
// as an effect, and the route asks the system.
class AssistantChatNotificationOffer(
	private val permission: NotificationPermissionRepository,
	private val offers: NotificationOfferRepository
) {
	suspend fun isOfferNeeded(): Boolean = permission.isOfferNeeded()

	fun markOffered() = offers.setOffered(true)
}
