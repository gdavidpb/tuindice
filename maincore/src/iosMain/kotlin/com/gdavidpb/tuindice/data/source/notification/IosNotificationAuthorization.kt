package com.gdavidpb.tuindice.data.source.notification

import platform.UserNotifications.UNAuthorizationStatus
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional

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
