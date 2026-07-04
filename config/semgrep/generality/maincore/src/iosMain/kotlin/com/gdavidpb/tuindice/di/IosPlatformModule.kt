package com.gdavidpb.tuindice.di

import org.koin.dsl.module

// Control negativo: maincore es dueño legítimo del wiring iOS
// y está exento de koin-no-per-feature-platform-module por paths.
val iosPlatformModule = module {
	single { "ios" }
}
