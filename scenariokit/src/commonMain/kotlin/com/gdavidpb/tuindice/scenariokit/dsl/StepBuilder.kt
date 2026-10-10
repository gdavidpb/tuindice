package com.gdavidpb.tuindice.scenariokit.dsl

import com.gdavidpb.tuindice.scenariokit.model.Site
import com.gdavidpb.tuindice.scenariokit.model.Step

/** Collects the steps of one block; every DSL function is an extension on it. */
@ScenarioDsl
open class StepBuilder {
	private val collected = mutableListOf<Step>()

	fun add(step: Step) {
		collected += step
	}

	/** Site of the scenario line calling the DSL. */
	fun site(): Site? = captureCallSite()

	internal fun build(): List<Step> = collected.toList()
}

internal fun collectSteps(block: StepBuilder.() -> Unit): List<Step> = StepBuilder().apply(block).build()
