package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.model.Step

/** Every step of a scenario, containers included, depth first. */
internal fun List<Step>.flattened(): List<Step> =
	flatMap { step -> listOf(step) + ((step as? Step.Container)?.steps?.flattened().orEmpty()) }
