package com.gdavidpb.tuindice.enrollmentproof.data.source

import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.testing.DEFAULT_ENROLLMENT_PROOF_CONTENT
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileKitStorageDataSourceContractTest {
	private val dataSource = FileKitStorageDataSource()

	@Test
	fun enrollmentProofExists_returnsFalseBeforeSaveAndTrueAfterSave() = runTest {
		val proofName = uniqueProofName("exists")

		try {
			assertFalse(dataSource.enrollmentProofExists(proofName))

			dataSource.saveEnrollmentProof(
				name = proofName,
				enrollmentProof = EnrollmentProof(
					source = "ignored",
					content = DEFAULT_ENROLLMENT_PROOF_CONTENT
				)
			)

			assertTrue(dataSource.enrollmentProofExists(proofName))
		} finally {
			deleteProofIfExists(proofName)
		}
	}

	@Test
	fun getEnrollmentProof_returnsPersistedContentAndResolvedLocalPath() = runTest {
		val proofName = uniqueProofName("read")

		try {
			dataSource.saveEnrollmentProof(
				name = proofName,
				enrollmentProof = EnrollmentProof(
					source = "remote-source.pdf",
					content = DEFAULT_ENROLLMENT_PROOF_CONTENT
				)
			)

			val enrollmentProof = dataSource.getEnrollmentProof(proofName)

			assertEquals(DEFAULT_ENROLLMENT_PROOF_CONTENT, enrollmentProof.content)
			assertTrue(enrollmentProof.source.endsWith("enrollmentProofs/$proofName.pdf"))
		} finally {
			deleteProofIfExists(proofName)
		}
	}

	private suspend fun deleteProofIfExists(name: String) {
		val file = FileKit.filesDir / "enrollmentProofs" / "$name.pdf"
		if (file.exists()) file.delete(mustExist = false)
	}

	private fun uniqueProofName(prefix: String): String =
		"test_${prefix}_${Random.nextInt(1_000_000)}"
}
