@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.scenariokit.engine

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Current instant as ISO-8601 UTC, from the Kotlin standard library clock. */
internal fun systemNowIso(): String = Clock.System.now().toString()
