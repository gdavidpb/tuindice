package com.gdavidpb.tuindice.record.data.enrollmentproof

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.record.data.enrollmentproof.source.LocalDataSource
import com.gdavidpb.tuindice.record.data.enrollmentproof.source.RemoteDataSource
import com.gdavidpb.tuindice.record.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.record.domain.repository.EnrollmentProofRepository

class EnrollmentProofDataRepository(
	private val localDataSource: LocalDataSource,
	private val remoteDataSource: RemoteDataSource,
	private val networkRepository: NetworkRepository
) : EnrollmentProofRepository {
	override suspend fun getEnrollmentProof(uid: String, quarter: Quarter): EnrollmentProof {
		val isNetworkAvailable = networkRepository.isAvailable()
		val enrollmentProofExists = localDataSource.enrollmentProofExists(uid, quarter)

		if (isNetworkAvailable && !enrollmentProofExists)
			remoteDataSource.getEnrollmentProof().also { enrollmentProof ->
				localDataSource.saveEnrollmentProof(uid, quarter, enrollmentProof)
			}

		return localDataSource.getEnrollmentProof(uid, quarter)
	}
}