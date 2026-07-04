// Fixture de semgrep --test para koin-conventions.yaml.
// No es código del proyecto: cada línea anotada valida una regla.
package com.gdavidpb.tuindice.sample.di

import org.koin.dsl.module

// ruleid: koin-no-empty-module
val emptyModule = module {}

// ruleid: koin-no-coremodule
val sampleCoreModule = module {
	single { "core" }
}

// ruleid: koin-no-per-feature-platform-module
val sampleAndroidModule = module {
	single { "android" }
}

// ruleid: koin-no-per-feature-platform-module
val sampleIosModule = module {
	single { "ios" }
}

// ok: koin-no-empty-module
val sampleModule = module {
	single { "sample" }
}

fun single(block: () -> String): String = block()
