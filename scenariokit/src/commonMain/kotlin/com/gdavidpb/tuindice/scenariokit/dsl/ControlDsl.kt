package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.Timeouts
import kotlin.time.Duration

/** Runs [block] only if [tag] shows up within [within]; looking never fails the scenario. */
fun StepBuilder.ifVisible(tag: String, within: Duration = Timeouts.Probe.asDuration(), block: StepBuilder.() -> Unit) =
	add(Step.IfVisible(Query.Tag(tag), within.millis(), collectSteps(block), site()))

/** Runs [block] only if [tag] is gone within [within]; looking never fails the scenario. */
fun StepBuilder.ifGone(tag: String, within: Duration = Timeouts.Probe.asDuration(), block: StepBuilder.() -> Unit) =
	add(Step.IfGone(Query.Tag(tag), within.millis(), collectSteps(block), site()))

fun StepBuilder.onPlatform(platform: Platform, block: StepBuilder.() -> Unit) =
	add(Step.OnPlatform(platform, collectSteps(block), site()))

/** Runs [block] up to [maxAttempts] (at most 3) times; every use needs a [reason]. */
fun StepBuilder.retry(maxAttempts: Int, reason: String, block: StepBuilder.() -> Unit) {
	require(
		maxAttempts in 1..Step.Retry.MAX_ATTEMPTS
	) { "retry allows 1..${Step.Retry.MAX_ATTEMPTS} attempts, got $maxAttempts" }
	require(reason.isNotBlank()) { "retry needs a reason" }
	add(Step.Retry(maxAttempts, reason, collectSteps(block), site()))
}

/** A named sub-scenario. */
fun StepBuilder.group(name: String, block: StepBuilder.() -> Unit) =
	add(Step.Group(name, collectSteps(block), site()))
