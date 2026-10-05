// Fixture de semgrep --test para infrastructure.yaml.
// No es código del proyecto: cada línea anotada valida una regla.
// Las exenciones por paths (persistence/, viewmodel/, di/, base/, testkit/) se
// validan en la prueba de generalidad (en --test los paths no aplican).
package com.gdavidpb.tuindice.sample.data.source

// ruleid: eventpublisher-only-at-mvi-boundary
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
// ok: eventpublisher-only-at-mvi-boundary
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
// ruleid: no-database-outside-persistence
import com.gdavidpb.tuindice.persistence.TuIndiceDatabase

class SampleTelemetryDataSource(
	private val eventPublisher: EventPublisher,
	// ruleid: no-database-outside-persistence
	private val database: TuIndiceDatabase,
	private val reportingRepository: ReportingRepository
) {
	fun log(value: String) {
		// ruleid: no-println
		println(value)
		// ok: no-println
		reportingRepository.setCustomKey("value", value)
	}
}

// ruleid: data-source-memory-is-session-memory
class SampleSelectionDataSource(
	private val settings: Settings
) : SampleSelectionRepository {
	private val selectedKey = MutableStateFlow(settings.getStringOrNull("selectedKey"))
}

// ruleid: data-source-memory-is-session-memory
class SampleCredentialsDataSource : SampleCredentialsRepository {
	private var memoryPassword: String? = null
}

// ruleid: data-source-memory-is-session-memory
class SampleCacheDataSource(
	private val dao: SampleDao
) : SampleCacheRepository {
	private val writeMutex = Mutex()
	private val cache = mutableMapOf<String, String>()
}

// ok: data-source-memory-is-session-memory
class SampleClearedSelectionDataSource(
	private val settings: Settings
) : SampleSelectionRepository, SessionMemory {
	private val selectedKey = MutableStateFlow(settings.getStringOrNull("selectedKey"))

	override suspend fun clearSessionMemory() {
		selectedKey.value = settings.getStringOrNull("selectedKey")
	}
}

// ok: data-source-memory-is-session-memory
class SampleStatelessDataSource(
	private val dao: SampleDao
) : SampleCacheRepository {
	private val writeMutex = Mutex()

	suspend fun read(key: String): String? {
		var result: String? = null
		val seen = mutableListOf<String>()

		return result
	}
}
