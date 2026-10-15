package com.gdavidpb.tuindice.sampleflow.di

import org.koin.dsl.module

val brokenModule = module {}

val sampleFlowCoreModule = module {
	single { "core" }
}

val sampleFlowAndroidModule = module {
	single { "platform" }
}

// Disparo de koin-no-getornull: un binding ausente devolvería null en silencio.
val sampleFlowOptionalModule = module {
	single { getOrNull<String>() }
}
