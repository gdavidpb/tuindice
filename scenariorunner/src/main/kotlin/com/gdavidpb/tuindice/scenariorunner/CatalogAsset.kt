package com.gdavidpb.tuindice.scenariorunner

import androidx.test.platform.app.InstrumentationRegistry
import com.gdavidpb.tuindice.scenariokit.codec.CatalogCodec
import com.gdavidpb.tuindice.scenariokit.model.Platform

/** The versioned catalog, packaged as an asset of the test APK. */
object CatalogAsset {
	private const val FILE = "scenarios.json"

	val json: String by lazy {
		InstrumentationRegistry.getInstrumentation().context.assets.open(FILE).bufferedReader().use { it.readText() }
	}

	/** Ids of the Android scenarios that pass the run's `scenario` and `scenarioModule` filters. */
	fun selectedIds(): List<String> {
		val wanted = RunConfig.scenarioIds
		val module = RunConfig.scenarioModule
		return CatalogCodec.decode(json).scenarios
			.filter { Platform.Android in it.platforms }
			.filter { wanted.isEmpty() || it.id in wanted }
			.filter { module == null || it.module == module }
			.map { it.id }
	}
}
