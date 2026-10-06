package com.gdavidpb.tuindice.scenariokit.codec

import com.gdavidpb.tuindice.scenariokit.model.Step
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Hash of a scenario's step tree without the `site` of each step, so moving a line in
 * the Kotlin source does not mark the scenario as changed.
 */
internal object StepsHash {
	private val json = Json { encodeDefaults = true }

	fun of(steps: List<Step>): String {
		val tree = json.encodeToJsonElement(ListSerializer(Step.serializer()), steps)
		return Fnv1a.hex(CanonicalJson.sorted(tree, without = setOf("site")).toString())
	}
}
