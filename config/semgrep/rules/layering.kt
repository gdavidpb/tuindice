// Fixture de semgrep --test para layering.yaml.
// No es código del proyecto: cada línea anotada valida una regla.
// Nota: en modo --test los filtros paths no aplican, por eso reglas de
// domain/** y ui/** se validan aquí juntas (líneas compartidas usan comas).
package com.gdavidpb.tuindice.sample.domain.usecase

// ruleid: domain-no-data-import, ui-no-business-import
import com.gdavidpb.tuindice.record.data.model.QuarterEntity
// ruleid: domain-no-data-import, ui-no-business-import
import com.gdavidpb.tuindice.data.source.sync.SyncDataSource
// ok: domain-no-data-import
import com.gdavidpb.tuindice.record.domain.model.Quarter
// ruleid: ui-no-business-import
import com.gdavidpb.tuindice.sample.domain.usecase.LoadThingUseCase
// ruleid: ui-no-business-import
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
// ok: ui-no-business-import
import com.gdavidpb.tuindice.about.presentation.contract.About
// ruleid: ui-no-koin-import
import org.koin.compose.viewmodel.koinViewModel
// ok: ui-no-koin-import
import kotlinx.coroutines.flow.Flow

// ruleid: no-datasource-interface
interface SampleDataSource {
	fun read(): String
}

// ruleid: no-datasource-interface
internal interface RemoteThingDataSource

// ok: no-datasource-interface
interface SampleRepository {
	fun read(): String
}

// ok: no-datasource-interface
interface SampleDataRepository {
	fun read(): String
}

// ruleid: data-repository-interfaces-only
class SampleOrchestrator(private val repository: SampleRepository)

// ruleid: data-repository-interfaces-only
data class SampleDto(val id: String)

// ruleid: data-repository-interfaces-only
object SampleCache

// ruleid: data-repository-interfaces-only
enum class SampleOrigin { LOCAL, REMOTE }

// ok: data-repository-interfaces-only
interface OtherDataRepository {
	fun read(): String
}
