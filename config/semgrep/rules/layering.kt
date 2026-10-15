// Fixture de semgrep --test para layering.yaml.
// No es código del proyecto: cada línea anotada valida una regla.
// Nota: en modo --test los filtros paths no aplican, por eso reglas de
// domain/**, ui/** y presentation/** se validan aquí juntas (líneas compartidas usan comas).
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
// ruleid: presentation-no-compose-render-import
import androidx.compose.ui.graphics.ImageBitmap
// ok: presentation-no-compose-render-import
import androidx.compose.ui.graphics.Color
// ok: presentation-no-compose-render-import
import androidx.compose.ui.graphics.vector.ImageVector
// ok: presentation-no-compose-render-import
import androidx.compose.ui.text.AnnotatedString
// ruleid: presentation-no-compose-render-import
import androidx.compose.ui.text.TextStyle
// ruleid: presentation-no-ui-layer-import
import com.gdavidpb.tuindice.sample.ui.view.SampleCardView
// ok: presentation-no-ui-layer-import
import com.gdavidpb.tuindice.sample.ui.model.SampleLabel
// ok: presentation-no-ui-layer-import
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
// ruleid: presentation-no-ui-layer-import
import com.gdavidpb.tuindice.base.ui.view.DropdownMenuItem
// ruleid: presentation-no-persistence-import
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
// ok: presentation-no-persistence-import
import com.gdavidpb.tuindice.sample.domain.model.PensumCreditsProgress

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
