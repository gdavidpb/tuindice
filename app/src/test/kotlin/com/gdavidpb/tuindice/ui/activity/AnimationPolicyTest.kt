package com.gdavidpb.tuindice.ui.activity

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnimationPolicyTest {
	@Test
	fun when_animatorScaleIsZero_then_animationsAreDisabled() {
		assertFalse(animationsEnabled(animatorDurationScale = 0f, forcedOff = false))
	}

	@Test
	fun when_animatorScaleIsNormal_then_animationsAreEnabled() {
		assertTrue(animationsEnabled(animatorDurationScale = 1f, forcedOff = false))
	}

	@Test
	fun when_animatorScaleIsSlowedDown_then_animationsAreStillEnabled() {
		assertTrue(animationsEnabled(animatorDurationScale = 10f, forcedOff = false))
	}

	@Test
	fun when_hostForcesAnimationsOff_then_theyAreDisabledEvenAtNormalScale() {
		assertFalse(animationsEnabled(animatorDurationScale = 1f, forcedOff = true))
	}
}
