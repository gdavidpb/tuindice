package com.gdavidpb.tuindice.summary.data.source

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AndroidBitmapPictureEncoderDataSourceTest {
    private val dataSource = AndroidBitmapPictureEncoderDataSource()

    @Test
    fun encodeEncodesJpegWhenDecodedImageHasPositiveDimensions() = runTest {
        val inputFile = createInputFile(
            prefix = "valid",
            extension = "png",
            content = createPngBytes(width = SmallImage.WIDTH, height = SmallImage.HEIGHT)
        )

        try {
            val encodedImage = dataSource.encodePicture(file = PlatformFile(inputFile))

            assertEquals(JPEG_MIME_TYPE, encodedImage.mimeType)
            assertTrue(encodedImage.content.isNotEmpty())
            assertTrue(encodedImage.content.size <= MAX_UPLOAD_BYTES)
            assertEncodedImageDimensions(
                content = encodedImage.content,
                expectedWidth = SmallImage.WIDTH,
                expectedHeight = SmallImage.HEIGHT
            )
        } finally {
            inputFile.delete()
        }
    }

    @Test
    fun encodeResizesBeforeEncodingWhenImageExceedsMaxDimension() = runTest {
        val inputFile = createInputFile(
            prefix = "large",
            extension = "png",
            content = createPngBytes(width = LargeImage.WIDTH, height = LargeImage.HEIGHT)
        )

        try {
            val encodedImage = dataSource.encodePicture(file = PlatformFile(inputFile))

            assertEquals(JPEG_MIME_TYPE, encodedImage.mimeType)
            assertTrue(encodedImage.content.isNotEmpty())
            assertTrue(encodedImage.content.size <= MAX_UPLOAD_BYTES)
            assertEncodedImageDimensions(
                content = encodedImage.content,
                expectedWidth = LargeImage.EXPECTED_WIDTH,
                expectedHeight = LargeImage.EXPECTED_HEIGHT
            )
        } finally {
            inputFile.delete()
        }
    }

    private fun assertEncodedImageDimensions(
        content: ByteArray,
        expectedWidth: Int,
        expectedHeight: Int
    ) {
        val decodedImage = assertNotNull(BitmapFactory.decodeByteArray(content, 0, content.size))

        try {
            assertEquals(expectedWidth, decodedImage.width)
            assertEquals(expectedHeight, decodedImage.height)
        } finally {
            decodedImage.recycle()
        }
    }

    private fun createPngBytes(width: Int, height: Int): ByteArray {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        try {
            bitmap.eraseColor(Color.rgb(TEST_COLOR_RED, TEST_COLOR_GREEN, TEST_COLOR_BLUE))

            val output = ByteArrayOutputStream()
            check(bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, output)) {
                "Expected a PNG-encoded image for tests."
            }

            return output.toByteArray()
        } finally {
            bitmap.recycle()
        }
    }

    private fun createInputFile(
        prefix: String,
        extension: String,
        content: ByteArray
    ): File {
        return File.createTempFile("${prefix}_", ".$extension").apply {
            writeBytes(content)
            deleteOnExit()
        }
    }

    private object SmallImage {
        const val WIDTH = 128
        const val HEIGHT = 128
    }

    private object LargeImage {
        const val WIDTH = 2048
        const val HEIGHT = 1024
        const val EXPECTED_WIDTH = 1024
        const val EXPECTED_HEIGHT = 512
    }

    private companion object {
        const val JPEG_MIME_TYPE = "image/jpeg"
        const val MAX_UPLOAD_BYTES = 1_048_576
        const val PNG_QUALITY = 100
        const val TEST_COLOR_RED = 68
        const val TEST_COLOR_GREEN = 119
        const val TEST_COLOR_BLUE = 153
    }
}
