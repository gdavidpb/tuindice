package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Step

fun StepBuilder.tap(tag: String) = add(Step.Tap(Query.Tag(tag), true, site()))

fun StepBuilder.tap(query: Query, requireEnabled: Boolean = true) = add(Step.Tap(query, requireEnabled, site()))

/** Taps at a screen fraction relative to the element. */
fun StepBuilder.tapAt(tag: String, fx: Double, fy: Double) = add(Step.TapAt(Query.Tag(tag), fx, fy, site()))

fun StepBuilder.tapAtScreen(fx: Double, fy: Double) = add(Step.TapAt(null, fx, fy, site()))

fun StepBuilder.doubleTap(tag: String) = add(Step.DoubleTap(Query.Tag(tag), site()))

fun StepBuilder.back() = add(Step.Back(site()))
