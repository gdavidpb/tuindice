package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.Timeouts
import kotlin.time.Duration

fun StepBuilder.waitVisible(tag: String, timeout: Duration = Timeouts.Wait.asDuration()) =
	add(Step.WaitVisible(Query.Tag(tag), timeout.millis(), site()))

fun StepBuilder.waitVisible(query: Query, timeout: Duration = Timeouts.Wait.asDuration()) =
	add(Step.WaitVisible(query, timeout.millis(), site()))

fun StepBuilder.waitGone(tag: String, timeout: Duration = Timeouts.Wait.asDuration()) =
	add(Step.WaitGone(Query.Tag(tag), timeout.millis(), site()))

fun StepBuilder.waitGone(query: Query, timeout: Duration = Timeouts.Wait.asDuration()) =
	add(Step.WaitGone(query, timeout.millis(), site()))

fun StepBuilder.waitAnyVisible(vararg queries: Query, timeout: Duration = Timeouts.Wait.asDuration()) =
	add(Step.WaitAnyVisible(queries.toList(), timeout.millis(), site()))

fun StepBuilder.assertEnabled(tag: String, enabled: Boolean = true, timeout: Duration = Timeouts.Assert.asDuration()) =
	add(Step.AssertEnabled(Query.Tag(tag), enabled, timeout.millis(), site()))

/** Waits for the element (the screen when [tag] is null) to stop moving; use before gestures by position. */
fun StepBuilder.settle(tag: String? = null, timeout: Duration = Timeouts.Action.asDuration()) =
	add(Step.Settle(tag?.let { Query.Tag(it) }, timeout.millis(), site()))
