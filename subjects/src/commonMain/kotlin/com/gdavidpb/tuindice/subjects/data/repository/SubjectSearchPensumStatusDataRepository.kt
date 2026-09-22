package com.gdavidpb.tuindice.subjects.data.repository

import com.gdavidpb.tuindice.subjects.data.model.SubjectSearchPensumStatusResolver
import kotlinx.coroutines.flow.Flow

interface SubjectSearchPensumStatusDataRepository {
	fun observePensumStatusResolver(): Flow<SubjectSearchPensumStatusResolver>
}
