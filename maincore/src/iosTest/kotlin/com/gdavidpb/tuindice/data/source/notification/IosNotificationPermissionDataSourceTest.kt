package com.gdavidpb.tuindice.data.source.notification

import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusEphemeral
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IosNotificationPermissionDataSourceTest {
	@Test
	fun offer_isNeeded_whenTheAuthorizationIsUndetermined_andNotOfferedYet() {
		assertTrue(notificationOfferNeeded(IosNotificationAuthorization.NOT_DETERMINED, isOffered = false))
	}

	@Test
	fun offer_isNotNeeded_whenTheStudentAlreadyAnsweredTheSystem() {
		assertFalse(notificationOfferNeeded(IosNotificationAuthorization.AUTHORIZED, isOffered = false))
		assertFalse(notificationOfferNeeded(IosNotificationAuthorization.DENIED, isOffered = false))
		assertFalse(notificationOfferNeeded(IosNotificationAuthorization.PROVISIONAL, isOffered = false))
		assertFalse(notificationOfferNeeded(IosNotificationAuthorization.EPHEMERAL, isOffered = false))
		assertFalse(notificationOfferNeeded(IosNotificationAuthorization.UNKNOWN, isOffered = false))
	}

	@Test
	fun offer_isNotNeeded_whenTheDeviceAlreadyOfferedIt() {
		assertFalse(notificationOfferNeeded(IosNotificationAuthorization.NOT_DETERMINED, isOffered = true))
	}

	@Test
	fun authorizationOf_readsEachStatusOfTheSystem() {
		val expected = listOf(
			UNAuthorizationStatusNotDetermined to IosNotificationAuthorization.NOT_DETERMINED,
			UNAuthorizationStatusDenied to IosNotificationAuthorization.DENIED,
			UNAuthorizationStatusAuthorized to IosNotificationAuthorization.AUTHORIZED,
			UNAuthorizationStatusProvisional to IosNotificationAuthorization.PROVISIONAL,
			UNAuthorizationStatusEphemeral to IosNotificationAuthorization.EPHEMERAL
		)

		expected.forEach { (status, authorization) ->
			assertEquals(authorization, IosNotificationAuthorization.of(status))
		}
	}
}
