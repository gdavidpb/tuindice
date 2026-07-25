package com.gdavidpb.tuindice.persistence.domain.repository

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import kotlinx.coroutines.flow.Flow

interface VisibleAcademicRecordRepository {
	fun observeVisibleAcademicRecordFlow(): Flow<AcademicRecord?>
}
