package com.gdavidpb.tuindice.summary.data.repository

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.ProfileIdentity
import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataRepository
import com.gdavidpb.tuindice.summary.data.source.ProfileIdentityDataSource
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.FakeSettingsDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProfileIdentityRepositoryContractTest {
	@Test
	fun observeProfileIdentity_namesTheStudentTheWayTheSummaryDoes() = runTest {
		val repository = ProfileIdentityDataSource(
			localDataSource = StoredUser(DEFAULT_SUMMARY_USER),
			settingsDataSource = FakeSettingsDataSource(onCooldown = false)
		)

		assertEquals(
			ProfileIdentity(
				name = "Ana Diaz",
				careerName = "Ingenieria Civil Informatica",
				carnet = "20261234",
				pictureUrl = "https://cdn.tuindice.app/profile/original.jpg",
				pictureVersion = 0
			),
			repository.observeProfileIdentity().first()
		)
	}

	@Test
	fun observeProfileIdentity_rollsThePictureVersionWithoutANewUser() = runTest {
		val settings = FakeSettingsDataSource(onCooldown = false)
		val repository = ProfileIdentityDataSource(
			localDataSource = StoredUser(DEFAULT_SUMMARY_USER),
			settingsDataSource = settings
		)

		repository.observeProfileIdentity().test {
			assertEquals(0, awaitItem()?.pictureVersion)

			settings.bumpProfilePictureVersion()

			assertEquals(1, awaitItem()?.pictureVersion)
		}
	}

	@Test
	fun observeProfileIdentity_isNullWhenNoUserIsStored_soNothingOfAPreviousAccountStays() = runTest {
		val stored = StoredUser(DEFAULT_SUMMARY_USER)
		val repository = ProfileIdentityDataSource(
			localDataSource = stored,
			settingsDataSource = FakeSettingsDataSource(onCooldown = false)
		)

		repository.observeProfileIdentity().test {
			assertEquals("Ana Diaz", awaitItem()?.name)

			stored.user.value = null

			assertNull(awaitItem())
		}
	}

	@Test
	fun observeProfileIdentity_keepsAnEmptyPictureUrlAsTheStudentHasNoPicture() = runTest {
		val repository = ProfileIdentityDataSource(
			localDataSource = StoredUser(DEFAULT_SUMMARY_USER.copy(pictureUrl = "")),
			settingsDataSource = FakeSettingsDataSource(onCooldown = false)
		)

		assertEquals("", repository.observeProfileIdentity().first()?.pictureUrl)
	}
}

private class StoredUser(initial: User?) : LocalDataRepository {
	val user = MutableStateFlow(initial)

	override fun getUserFlow(): Flow<User?> = user

	override suspend fun updateUser(user: User) {
		this.user.value = user
	}
}
