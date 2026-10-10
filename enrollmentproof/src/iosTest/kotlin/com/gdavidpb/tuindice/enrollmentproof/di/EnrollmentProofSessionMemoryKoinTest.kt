package com.gdavidpb.tuindice.enrollmentproof.di

import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.base.domain.session.SessionResidue
import com.gdavidpb.tuindice.enrollmentproof.data.source.FileKitStorageDataSource
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The sign-out wipe finds the saved proofs only through the `SessionMemory` binding, and the start
 * without a session only through the `SessionResidue` one. They resolve the holder, which needs
 * FileKit initialised, so they run where the storage tests do: on iOS.
 */
class EnrollmentProofSessionMemoryKoinTest {
	@Test
	fun bindsTheProofStorageAsSessionMemory() = withKoinSmokeTest(enrollmentProofModule) {
		assertTrue(getAll<SessionMemory>().any { holder -> holder is FileKitStorageDataSource })
	}

	@Test
	fun bindsTheProofStorageAsSessionResidue() = withKoinSmokeTest(enrollmentProofModule) {
		assertTrue(getAll<SessionResidue>().any { holder -> holder is FileKitStorageDataSource })
	}
}
