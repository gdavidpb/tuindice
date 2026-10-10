package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.Timeouts
import kotlin.time.Duration

/** Runs [block] only if [tag] shows up within [within]; looking never fails the scenario. */
fun StepBuilder.ifVisible(tag: String, within: Duration = Timeouts.Probe.asDuration(), block: StepBuilder.() -> Unit) =
	add(Step.IfVisible(Query.Tag(tag), within.millis(), collectSteps(block), site()))

/** Same as the tag overload, for an element found by text or by OS-level label. */
fun StepBuilder.ifVisible(query: Query, within: Duration = Timeouts.Probe.asDuration(), block: StepBuilder.() -> Unit) =
	add(Step.IfVisible(query, within.millis(), collectSteps(block), site()))

fun StepBuilder.onPlatform(platform: Platform, block: StepBuilder.() -> Unit) =
	add(Step.OnPlatform(platform, collectSteps(block), site()))

/** A named sub-scenario. */
fun StepBuilder.group(name: String, block: StepBuilder.() -> Unit) =
	add(Step.Group(name, collectSteps(block), site()))
