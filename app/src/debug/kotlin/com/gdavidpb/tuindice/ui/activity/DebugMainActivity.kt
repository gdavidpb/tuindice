package com.gdavidpb.tuindice.ui.activity

import com.gdavidpb.tuindice.e2e.E2eSeedBridge

class DebugMainActivity : MainActivity() {
	override fun onBeforeContent() {
		E2eSeedBridge.seedIfRequested(activity = this, intent = intent)
	}
}
