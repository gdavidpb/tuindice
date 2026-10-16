package com.gdavidpb.tuindice.summary.data.source

import android.content.Intent
import android.net.Uri
import com.gdavidpb.tuindice.summary.domain.model.ShareableImage
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import kotlinx.coroutines.Dispatchers
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AndroidImageShareDataSourceTest {
	private val context = RuntimeEnvironment.getApplication()

	private val dataSource = AndroidImageShareDataSource(
		context = context,
		dispatchers = TestTuIndiceDispatchers(Dispatchers.Unconfined)
	)

	@Test
	fun writeImageKeepsTheBytesUnderTheShareDirectoryOfTheCache() {
		val file = dataSource.writeImage(ShareableImage(content = PNG_BYTES, fileName = "statistics.png"))

		assertEquals(File(context.cacheDir, IMAGE_SHARE_DIRECTORY_NAME), file.parentFile)
		assertContentEquals(PNG_BYTES, file.readBytes())
	}

	@Test
	fun writeImageReplacesTheImageWrittenBefore() {
		dataSource.writeImage(ShareableImage(content = PNG_BYTES, fileName = "first.png"))

		val second = dataSource.writeImage(ShareableImage(content = PNG_BYTES, fileName = "second.png"))

		assertEquals(listOf("second.png"), second.parentFile?.list()?.toList())
	}

	@Test
	fun launchShareSheetStartsAChooserWithTheImageAsAReadableStream() {
		val uri = Uri.parse("content://${context.packageName}/$IMAGE_SHARE_DIRECTORY_NAME/statistics.png")

		dataSource.launchShareSheet(uri)

		val chooser = shadowOf(context).nextStartedActivity
		val sendIntent = assertIs<Intent>(chooser.extras?.get(Intent.EXTRA_INTENT))

		assertEquals(Intent.ACTION_CHOOSER, chooser.action)
		assertTrue((chooser.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)
		assertEquals(Intent.ACTION_SEND, sendIntent.action)
		assertEquals("image/png", sendIntent.type)
		assertEquals(uri, sendIntent.extras?.get(Intent.EXTRA_STREAM))
		assertEquals(uri, sendIntent.clipData?.getItemAt(0)?.uri)
		assertTrue((sendIntent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0)
	}

	private companion object {
		// The signature of a PNG, which is all a share needs to hand the bytes over intact.
		val PNG_BYTES = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
	}
}
