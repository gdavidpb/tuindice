package com.gdavidpb.tuindice.data.source.functions

import com.gdavidpb.tuindice.base.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.repository.ServicesRepository
import com.gdavidpb.tuindice.base.utils.extensions.getOrThrow
import com.gdavidpb.tuindice.data.source.functions.mappers.toEnrollmentProof
import com.gdavidpb.tuindice.data.source.functions.mappers.toQuarter

class CloudFunctionsDataSource(
	private val tuIndiceAPI: TuIndiceAPI
) : ServicesRepository {
	override suspend fun getQuarters(): List<Quarter> {
		return tuIndiceAPI.getQuarters()
			.getOrThrow()
			.map { quarterResponse -> quarterResponse.toQuarter() }
	}

	override suspend fun getEnrollmentProof(): EnrollmentProof {
		return tuIndiceAPI.enrollmentProof()
			.getOrThrow()
			.toEnrollmentProof()
	}
}