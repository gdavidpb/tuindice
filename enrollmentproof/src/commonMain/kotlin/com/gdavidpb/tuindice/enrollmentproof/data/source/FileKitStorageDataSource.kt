package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.enrollmentproof.data.repository.StorageDataRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import io.github.vinceglb.filekit.*
import kotlin.io.encoding.Base64

class FileKitStorageDataSource : StorageDataRepository {
	private val enrollmentProofDir = FileKit.filesDir / "enrollmentProofs"

	override suspend fun getEnrollmentProof(name: String): EnrollmentProof {
		val enrollmentProofFile = enrollmentProofFile(name)

		return EnrollmentProof(
			source = enrollmentProofFile.path,
			content = Base64.encode(enrollmentProofFile.readBytes())
		)
	}

	override suspend fun enrollmentProofExists(name: String): Boolean {
		return enrollmentProofFile(name).exists()
	}

	override suspend fun saveEnrollmentProof(name: String, enrollmentProof: EnrollmentProof) {
		enrollmentProofDir.createDirectories()
		enrollmentProofFile(name).write(Base64.decode(enrollmentProof.content))
	}

	private fun enrollmentProofFile(name: String) =
		enrollmentProofDir / "$name.pdf"
}
