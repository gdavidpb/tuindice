package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.scenariokit.codec.CatalogCodec
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Round-trips the catalog and writes the generated artifacts under `build/e2e/catalog`;
 * `./gradlew syncE2eArtifacts` copies them into the repo and `verifyE2eArtifactsFresh` compares them.
 */
class CatalogExportTest {
	private val outputDir = File("build/e2e/catalog")

	@Test
	fun theCodecRoundTripsTheCatalog() {
		val catalog = E2eCatalog.catalog()

		assertEquals(catalog, CatalogCodec.decode(CatalogCodec.encode(catalog)))
	}

	@Test
	fun theEncodingIsDeterministic() {
		assertEquals(CatalogCodec.encode(E2eCatalog.catalog()), CatalogCodec.encode(E2eCatalog.catalog()))
	}

	@Test
	fun writesTheCatalogJsonAndTheSwiftTests() {
		outputDir.mkdirs()

		File(outputDir, "scenarios.json").writeText(CatalogCodec.encode(E2eCatalog.catalog()))
		File(outputDir, "ScenarioTests.generated.swift").writeText(SwiftScenarioTests.render(E2eCatalog.all))

		assertTrue(File(outputDir, "scenarios.json").length() > 0)
		assertTrue(File(outputDir, "ScenarioTests.generated.swift").length() > 0)
	}
}
