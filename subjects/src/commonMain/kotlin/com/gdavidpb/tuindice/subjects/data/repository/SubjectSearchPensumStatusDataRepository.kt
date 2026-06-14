package com.gdavidpb.tuindice.subjects.data.repository

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import kotlinx.coroutines.flow.Flow

interface SubjectSearchPensumStatusDataRepository {
	fun observeStatusBySubjectCode(): Flow<Map<String, AcademicPensumNodeStatus>>
}
