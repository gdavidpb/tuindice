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
