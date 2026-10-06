package com.gdavidpb.tuindice.ui.activity

import com.gdavidpb.tuindice.debug.DebugLaunchArguments
import com.gdavidpb.tuindice.e2e.E2eSeedBridge

class DebugMainActivity : MainActivity() {
	private var launchArguments: DebugLaunchArguments? = null

	override fun onBeforeContent() {
		launchArguments = E2eSeedBridge.applyLaunchArguments(intent)
	}

	override fun areAnimationsForcedOff(): Boolean {
		return launchArguments?.animationsDisabled == true
	}
}
