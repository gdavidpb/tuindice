package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.Query
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun tag(value: String): Query = Query.Tag(value)

fun text(value: String, contains: Boolean = false): Query = Query.Text(value, contains)

fun system(value: String): Query = Query.System(value)

internal fun Duration.millis(): Long = inWholeMilliseconds

internal fun Long.asDuration(): Duration = milliseconds
