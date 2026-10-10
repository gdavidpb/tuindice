package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.base.domain.session.SessionResidue
import com.gdavidpb.tuindice.enrollmentproof.data.repository.StorageDataRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import io.github.vinceglb.filekit.*
import kotlin.io.encoding.Base64

class FileKitStorageDataSource : StorageDataRepository, SessionMemory, SessionResidue {
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

	// The proofs are keyed by quarter, not by account, and the stores a sign-out wipes do not hold them.
	override suspend fun clearSessionMemory() {
		deleteEnrollmentProofs()
	}

	// What a version before the sign-out wipe left for the account that already left.
	override suspend fun clearSessionResidue() {
		deleteEnrollmentProofs()
	}

	private suspend fun deleteEnrollmentProofs() {
		if (!enrollmentProofDir.exists()) return

		// The directory holds only flat files, and FileKit refuses to delete a directory with content.
		enrollmentProofDir.list().forEach { proofFile -> proofFile.delete(mustExist = false) }
		enrollmentProofDir.delete(mustExist = false)
	}

	private fun enrollmentProofFile(name: String) =
		enrollmentProofDir / "$name.pdf"
}
