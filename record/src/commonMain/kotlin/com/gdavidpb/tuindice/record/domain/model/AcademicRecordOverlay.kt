package com.gdavidpb.tuindice.record.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride

/**
 * Aplica un override sobre el expediente, reemplazando el que hubiera para el mismo
 * intento. Es la misma operación que usa la proyección del outbox al leer, expuesta aquí
 * para que el valor en vuelo de un gesto la reutilice sin pasar por la capa de datos.
 */
fun AcademicRecord.applying(override: AttemptOverride): AcademicRecord {
	return copy(
		attemptOverrides = attemptOverrides
			.filterNot { current -> current.attemptId == override.attemptId } + override
	)
}
