package com.gdavidpb.tuindice.di

import kotlin.test.Test
import kotlin.test.assertFalse

class IosKoinTest {
	@Test
	fun restartIosKoinModules_returnsFalseWhenKoinHasNotStarted() {
		assertFalse(restartIosKoinModules())
	}
}
