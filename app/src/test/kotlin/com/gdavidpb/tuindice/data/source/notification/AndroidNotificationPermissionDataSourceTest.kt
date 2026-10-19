package com.gdavidpb.tuindice.data.source.notification

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidNotificationPermissionDataSourceTest {
	@Test
	fun offer_isNotNeeded_belowAndroid13_becauseTheSystemDoesNotAsk() {
		assertFalse(notificationOfferNeeded(sdkInt = 32, isGranted = false, isOffered = false))
	}

	@Test
	fun offer_isNotNeeded_whenThePermissionIsGranted() {
		assertFalse(notificationOfferNeeded(sdkInt = 33, isGranted = true, isOffered = false))
	}

	@Test
	fun offer_isNotNeeded_whenTheDeviceAlreadyOfferedIt() {
		assertFalse(notificationOfferNeeded(sdkInt = 33, isGranted = false, isOffered = true))
	}

	@Test
	fun offer_isNeeded_fromAndroid13_whenNotGrantedAndNotOfferedYet() {
		assertTrue(notificationOfferNeeded(sdkInt = 33, isGranted = false, isOffered = false))
		assertTrue(notificationOfferNeeded(sdkInt = 36, isGranted = false, isOffered = false))
	}
}
