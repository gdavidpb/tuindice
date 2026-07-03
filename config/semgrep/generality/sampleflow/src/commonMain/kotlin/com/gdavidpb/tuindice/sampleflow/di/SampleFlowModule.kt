package com.gdavidpb.tuindice.sampleflow.di

import org.koin.dsl.module

val brokenModule = module {}

val sampleFlowCoreModule = module {
	single { "core" }
}

val sampleFlowAndroidModule = module {
	single { "platform" }
}
