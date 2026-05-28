@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.base.utils

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
