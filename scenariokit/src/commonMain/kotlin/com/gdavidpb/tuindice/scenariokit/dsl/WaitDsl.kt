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

/** Waits until the app has left the foreground (an external link, a system sheet that takes the front). */
fun StepBuilder.waitBackgrounded(timeout: Duration = Timeouts.Wait.asDuration()) =
	add(Step.WaitBackgrounded(timeout.millis(), site()))

/** Waits until the checkbox with [tag] shows the state [checked]. */
fun StepBuilder.assertChecked(tag: String, checked: Boolean, timeout: Duration = Timeouts.Assert.asDuration()) =
	add(Step.AssertChecked(Query.Tag(tag), checked, timeout.millis(), site()))

fun StepBuilder.assertEnabled(tag: String, enabled: Boolean = true, timeout: Duration = Timeouts.Assert.asDuration()) =
	add(Step.AssertEnabled(Query.Tag(tag), enabled, timeout.millis(), site()))
