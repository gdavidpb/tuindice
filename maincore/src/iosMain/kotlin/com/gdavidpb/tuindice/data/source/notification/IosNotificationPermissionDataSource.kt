package com.gdavidpb.tuindice.data.source.notification

import com.gdavidpb.tuindice.base.domain.repository.NotificationOfferRepository
import com.gdavidpb.tuindice.base.domain.repository.NotificationPermissionRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatus
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import kotlin.coroutines.resume

// iOS does not ask for the notification permission at launch: the chat's offer asks for it on the student's word, while
// the authorization is still undetermined. The push token is registered without it.
class IosNotificationPermissionDataSource(
	private val offers: NotificationOfferRepository
) : NotificationPermissionRepository {
	override suspend fun isOfferNeeded(): Boolean {
		return notificationOfferNeeded(
			authorization = currentAuthorization(),
			isOffered = offers.isOffered()
		)
	}

	override fun requestPermission() {
		UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
			options = UNAuthorizationOptionAlert or UNAuthorizationOptionBadge or UNAuthorizationOptionSound
		) { _, _ -> }
	}

	// The notification center answers on its own queue: the answer is taken back on the main queue.
	private suspend fun currentAuthorization(): IosNotificationAuthorization {
		return suspendCancellableCoroutine { continuation ->
			UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
				val authorization = IosNotificationAuthorization.of(settings?.authorizationStatus)

				dispatch_async(dispatch_get_main_queue()) {
					continuation.resume(authorization)
				}
			}
		}
	}
}

// What the system says of the student's authorization for notifications. A status this build does not know is none.
internal enum class IosNotificationAuthorization {
	NOT_DETERMINED,
	DENIED,
	AUTHORIZED,
	PROVISIONAL,
	EPHEMERAL,
	UNKNOWN;

	companion object {
		fun of(status: UNAuthorizationStatus?): IosNotificationAuthorization {
			return when (status) {
				UNAuthorizationStatusNotDetermined -> NOT_DETERMINED
				UNAuthorizationStatusDenied -> DENIED
				UNAuthorizationStatusAuthorized -> AUTHORIZED
				UNAuthorizationStatusProvisional -> PROVISIONAL
				UNAuthorizationStatusEphemeral -> EPHEMERAL
				else -> UNKNOWN
			}
		}
	}
}

// The card is offered only while the system has not answered yet, and once per device.
internal fun notificationOfferNeeded(authorization: IosNotificationAuthorization, isOffered: Boolean): Boolean {
	return authorization == IosNotificationAuthorization.NOT_DETERMINED && !isOffered
}
