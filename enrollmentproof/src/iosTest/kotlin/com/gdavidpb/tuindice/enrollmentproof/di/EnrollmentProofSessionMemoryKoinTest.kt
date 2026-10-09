package com.gdavidpb.tuindice.enrollmentproof.di

import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.enrollmentproof.data.source.FileKitStorageDataSource
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The sign-out wipe finds the saved proofs only through the `SessionMemory` binding. It resolves
 * the holder, which needs FileKit initialised, so it runs where the storage tests do: on iOS.
 */
class EnrollmentProofSessionMemoryKoinTest {
	@Test
	fun bindsTheProofStorageAsSessionMemory() = withKoinSmokeTest(enrollmentProofModule) {
		assertTrue(getAll<SessionMemory>().any { holder -> holder is FileKitStorageDataSource })
	}
}
