package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Step

/** Every step of a scenario, containers included, depth first. */
internal fun List<Step>.flattened(): List<Step> =
	flatMap { step -> listOf(step) + ((step as? Step.Container)?.steps?.flattened().orEmpty()) }

/** The steps that run on [platform], depth first: [flattened] without the branches of the other platform. */
internal fun List<Step>.reachableOn(platform: Platform): List<Step> =
	filterNot { it is Step.OnPlatform && it.platform != platform }
		.flatMap { step -> listOf(step) + ((step as? Step.Container)?.steps?.reachableOn(platform).orEmpty()) }
