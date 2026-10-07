@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.base.ui.style

import androidx.compose.runtime.compositionLocalOf
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * The clock that says what "today" is for what the screens show by date. The hosts provide the
 * clock Koin binds (the system one, or in debug builds the one `TUINDICE_E2E_NOW` can freeze);
 * the system clock is only the value for trees that no host builds, such as previews and tests.
 */
val LocalTuIndiceClock = compositionLocalOf<Clock> { Clock.System }
