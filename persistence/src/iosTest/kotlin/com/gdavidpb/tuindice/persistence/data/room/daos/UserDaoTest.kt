package com.gdavidpb.tuindice.persistence.data.room.daos

import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase
import com.gdavidpb.tuindice.persistence.data.room.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserDaoTest {
	private lateinit var database: TuIndiceDatabase
	private lateinit var dao: UserDao

	@BeforeTest
	fun setUp() {
		database = createInMemoryTuIndiceDatabase()
		dao = database.users
	}

	@AfterTest
	fun tearDown() {
		database.close()
	}

	@Test
	fun getUserFlow_whenEmpty_emitsNull() = runTest {
		assertNull(dao.getUserFlow().first())
	}

	@Test
	fun upsertEntity_thenGetUserFlow_returnsRoundTrippedUser() = runTest {
		val user = user()

		dao.upsertEntity(user)

		assertEquals(user, dao.getUserFlow().first())
	}

	@Test
	fun upsertEntity_withExistingId_replacesUser() = runTest {
		val original = user(grade = 4.25)
		val updated = original.copy(
			grade = 4.5,
			approvedSubjects = original.approvedSubjects + 1,
			lastUpdate = original.lastUpdate + 1L
		)

		dao.upsertEntity(original)
		dao.upsertEntity(updated)

		assertEquals(updated, dao.getUserFlow().first())
	}

	@Test
	fun updateProfilePicture_isVisibleThroughGetUserFlow() = runTest {
		val user = user(pictureUrl = "https://example.com/old.png")
		val newUrl = "https://example.com/new.png"

		dao.upsertEntity(user)
		dao.updateProfilePicture(url = newUrl)

		assertEquals(
			user.copy(pictureUrl = newUrl),
			dao.getUserFlow().first()
		)
	}

	@Test
	fun deleteAll_thenGetUserFlow_emitsNull() = runTest {
		dao.upsertEntity(user())

		dao.deleteAll()

		assertNull(dao.getUserFlow().first())
	}

	private fun user(
		id: String = "user-1",
		pictureUrl: String = "https://example.com/picture.png",
		grade: Double = 4.25
	) = UserEntity(
		id = id,
		cid = "V-12345678",
		usbId = "12-34567",
		email = "student@usb.ve",
		pictureUrl = pictureUrl,
		fullName = "John Doe",
		firstNames = "John",
		lastNames = "Doe",
		careerName = "Computer Engineering",
		careerCode = 800,
		scholarship = false,
		grade = grade,
		enrolledSubjects = 5,
		enrolledCredits = 20,
		approvedSubjects = 30,
		approvedCredits = 120,
		retiredSubjects = 1,
		retiredCredits = 4,
		failedSubjects = 2,
		failedCredits = 8,
		lastUpdate = 1700000000000L
	)
}
