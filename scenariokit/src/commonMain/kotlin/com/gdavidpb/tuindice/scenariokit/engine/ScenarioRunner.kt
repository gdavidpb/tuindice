package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.codec.CatalogCodec
import com.gdavidpb.tuindice.scenariokit.contract.DriverContract
import com.gdavidpb.tuindice.scenariokit.driver.ScenarioDriver
import com.gdavidpb.tuindice.scenariokit.model.ScenarioOutcome

/** The only entry point the platform runners call; none of its functions throws. */
object ScenarioRunner {
	fun run(catalogJson: String, scenarioId: String, driver: ScenarioDriver): ScenarioOutcome =
		runWith(catalogJson, scenarioId, driver, Clocks())

	internal fun runWith(
		catalogJson: String,
		scenarioId: String,
		driver: ScenarioDriver,
		clocks: Clocks
	): ScenarioOutcome =
		runCatching { ScenarioInterpreter(driver, clocks).run(CatalogCodec.decode(catalogJson).scenario(scenarioId)) }
			.getOrElse { ScenarioOutcome.crashed(scenarioId, it.stackTraceToString().take(STACK_LIMIT)) }

	/** Ids of every scenario in the catalog; empty when the catalog cannot be read. */
	fun ids(catalogJson: String): List<String> =
		runCatching { CatalogCodec.decode(catalogJson).scenarios.map { it.id } }.getOrDefault(emptyList())

	/** Probes [driver] with the catalog's contract fixture instead of running a scenario. */
	fun driverContract(catalogJson: String, driver: ScenarioDriver): ScenarioOutcome =
		driverContractWith(catalogJson, driver, Clocks())

	internal fun driverContractWith(catalogJson: String, driver: ScenarioDriver, clocks: Clocks): ScenarioOutcome =
		runCatching { DriverContract.run(CatalogCodec.decode(catalogJson).contractFixture, driver, clocks) }
			.getOrElse { ScenarioOutcome.crashed(DriverContract.ID, it.stackTraceToString().take(STACK_LIMIT)) }

	private const val STACK_LIMIT = 2_000
}
