package com.gdavidpb.tuindice.scenariokit.model

import com.gdavidpb.tuindice.scenariokit.codec.CatalogCodec
import com.gdavidpb.tuindice.scenariokit.contract.DriverContractFixture
import kotlinx.serialization.Serializable

@Serializable
data class ScenarioCatalog(
	val schema: String = CatalogCodec.SCHEMA,
	val accounts: List<CatalogAccount>,
	val scenarios: List<Scenario>,
	val contractFixture: DriverContractFixture
) {
	fun scenario(id: String): Scenario =
		scenarios.firstOrNull { it.id == id } ?: throw NoSuchElementException("Unknown scenario id: $id")
}
