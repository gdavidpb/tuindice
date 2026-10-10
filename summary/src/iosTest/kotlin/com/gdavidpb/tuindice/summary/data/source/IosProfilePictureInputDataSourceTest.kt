package com.gdavidpb.tuindice.summary.data.source

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse

/**
 * The normalized JPEG of a chosen photo is the person's own and outlives the upload only when the
 * process dies in between. A sign-out and a start without a session both have to remove it.
 */
class IosProfilePictureInputDataSourceTest {
	private val dataSource = IosProfilePictureInputDataSource()
	private val directory = FileKit.filesDir / "summaryProfilePictures"

	@Test
	fun clearSessionMemory_removesTheNormalizedPicturesAndTheirDirectory() = runTest {
		leaveANormalizedPicture("memory")

		dataSource.clearSessionMemory()

		assertFalse(directory.exists())
	}

	@Test
	fun clearSessionResidue_removesTheNormalizedPicturesAndTheirDirectory() = runTest {
		leaveANormalizedPicture("residue")

		dataSource.clearSessionResidue()

		assertFalse(directory.exists())
	}

	@Test
	fun clearing_withoutTheDirectory_doesNotFail() = runTest {
		dataSource.clearSessionMemory()
		dataSource.clearSessionResidue()
		dataSource.clearSessionResidue()

		assertFalse(directory.exists())
	}

	private suspend fun leaveANormalizedPicture(name: String) {
		directory.createDirectories()
		(directory / "profile_picture_$name.jpg").write(byteArrayOf(1, 2, 3))
		(directory / "profile_picture_${name}_2.jpg").write(byteArrayOf(4, 5, 6))
	}
}
