package com.gdavidpb.tuindice.summary.data.repository

import com.gdavidpb.tuindice.base.domain.model.ProfileIdentity
import com.gdavidpb.tuindice.summary.data.mapper.toProfileIdentity
import com.gdavidpb.tuindice.summary.data.source.ProfileIdentityDataSource
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileIdentityRepositoryContractTest {
	@Test
	fun observeProfileIdentity_namesTheStudentTheWayTheSummaryDoes() = runTest {
		val repository = ProfileIdentityDataSource(userRepository = RecordingUserRepository())

		val identity = repository.observeProfileIdentity().first()

		assertEquals(
			ProfileIdentity(
				name = "Ana Diaz",
				careerName = "Ingenieria Civil Informatica",
				carnet = "20261234",
				pictureUrl = "https://cdn.tuindice.app/profile/original.jpg",
				pictureVersion = 0
			),
			identity
		)
	}

	@Test
	fun observeProfileIdentity_followsTheStoredUserAndItsPictureVersion() = runTest {
		val reuploaded = DEFAULT_SUMMARY_USER.copy(pictureVersion = 3)
		val repository = ProfileIdentityDataSource(
			userRepository = RecordingUserRepository(users = flowOf(DEFAULT_SUMMARY_USER, reuploaded))
		)

		val identities = repository.observeProfileIdentity().toList()

		assertEquals(
			listOf(DEFAULT_SUMMARY_USER.toProfileIdentity(), reuploaded.toProfileIdentity()),
			identities
		)
		assertEquals(listOf(0, 3), identities.map { identity -> identity.pictureVersion })
	}

	@Test
	fun observeProfileIdentity_keepsAnEmptyPictureUrlAsTheStudentHasNoPicture() = runTest {
		val repository = ProfileIdentityDataSource(
			userRepository = RecordingUserRepository(users = flowOf(DEFAULT_SUMMARY_USER.copy(pictureUrl = "")))
		)

		assertEquals("", repository.observeProfileIdentity().first().pictureUrl)
	}
}
