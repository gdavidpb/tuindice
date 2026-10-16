package com.gdavidpb.tuindice.base.ui.icon

import kotlin.test.Test
import kotlin.test.assertEquals

class ShareIconAndroidTest {
	@Test
	fun showsMaterialsShareIconOnAndroid() {
		assertEquals("Outlined.Share", ShareIcon.name)
	}
}
