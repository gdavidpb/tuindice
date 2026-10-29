package com.gdavidpb.tuindice.base.data.source.machine

import com.gdavidpb.tuindice.base.domain.startup.AppStartupTask
import com.gdavidpb.tuindice.base.logging.MachineLog
import com.gdavidpb.tuindice.base.logging.appLogger

// Turns the state machines' log on in a debug build: each line goes to the app's logger under the "Machine" tag.
class DebugMachineLogDataSource : AppStartupTask {
	private val logger = appLogger(tag = "Machine")

	override fun start() {
		MachineLog.sink = { line -> logger.i { line } }
	}
}
