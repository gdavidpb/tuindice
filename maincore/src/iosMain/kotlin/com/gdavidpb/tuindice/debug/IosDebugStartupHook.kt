package com.gdavidpb.tuindice.debug

import org.koin.core.Koin

fun interface IosDebugStartupHook {
	suspend fun run(koin: Koin, mainSectionName: String)
}
