package com.gdavidpb.tuindice.base.ui.icon

import kotlin.test.Test
import kotlin.test.assertEquals

class ShareIconIosTest {
	@Test
	fun showsTheSystemsShareIconOnIos() {
		assertEquals("square.and.arrow.up", ShareIcon.name)
	}
}
