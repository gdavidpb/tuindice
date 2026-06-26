package com.gdavidpb.tuindice.summary.data.source

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.graphics.createBitmap
import com.gdavidpb.tuindice.base.domain.model.EncodedImage
import com.gdavidpb.tuindice.summary.data.repository.user.PictureEncoderDataRepository
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import java.io.ByteArrayOutputStream

class AndroidBitmapPictureEncoderDataSource : PictureEncoderDataRepository {
    private object Settings {
        const val JPEG_MIME_TYPE = "image/jpeg"
        const val JPEG_QUALITY = 85
        const val MAX_DIMENSION_PX = 1024
        const val MAX_UPLOAD_BYTES = 1_048_576
    }

    override suspend fun encodePicture(file: PlatformFile): EncodedImage {
        val content = file.readBytes()
        val bounds = decodeBounds(content)
        val targetSize = bounds.targetSize()
        val decodedBitmap = decodeBitmap(content, bounds.sampleSize())
        val normalizedBitmap = decodedBitmap.toOpaqueScaledBitmap(
            width = targetSize.width,
            height = targetSize.height
        )

        try {
            val encodedBytes = normalizedBitmap.encodeJpeg()

            check(encodedBytes.size <= Settings.MAX_UPLOAD_BYTES) {
                "Image exceeds maximum upload size."
            }

            return EncodedImage(
                content = encodedBytes,
                mimeType = Settings.JPEG_MIME_TYPE
            )
        } finally {
            normalizedBitmap.recycle()
            decodedBitmap.recycle()
        }
    }

    private fun decodeBounds(content: ByteArray): BitmapBounds {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeByteArray(content, 0, content.size, options)

        require(options.outWidth > 0 && options.outHeight > 0) {
            "Profile picture content is not a supported image."
        }

        return BitmapBounds(
            width = options.outWidth,
            height = options.outHeight
        )
    }

    private fun decodeBitmap(content: ByteArray, sampleSize: Int): Bitmap {
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inSampleSize = sampleSize
        }

        return BitmapFactory.decodeByteArray(content, 0, content.size, options)
            ?: throw IllegalArgumentException("Profile picture content is not a supported image.")
    }

    private fun Bitmap.toOpaqueScaledBitmap(width: Int, height: Int): Bitmap {
        val target = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)

        Canvas(target).apply {
            drawColor(Color.WHITE)
            drawBitmap(
                this@toOpaqueScaledBitmap,
                null,
                Rect(0, 0, width, height),
                paint
            )
        }

        return target
    }

    private fun Bitmap.encodeJpeg(): ByteArray {
        val output = ByteArrayOutputStream()
        check(compress(Bitmap.CompressFormat.JPEG, Settings.JPEG_QUALITY, output)) {
            "Image encoding returned no data."
        }

        return output.toByteArray()
    }

    private data class BitmapBounds(
        val width: Int,
        val height: Int
    ) {
        fun targetSize(): BitmapSize {
            val maxDimension = maxOf(width, height)
            val scaleFactor = if (maxDimension <= Settings.MAX_DIMENSION_PX) {
                1.0
            } else {
                Settings.MAX_DIMENSION_PX.toDouble() / maxDimension.toDouble()
            }

            return BitmapSize(
                width = maxOf(1, (width * scaleFactor).toInt()),
                height = maxOf(1, (height * scaleFactor).toInt())
            )
        }

        fun sampleSize(): Int {
            var sampleSize = 1
            val halfWidth = width / 2
            val halfHeight = height / 2

            while (maxOf(halfWidth / sampleSize, halfHeight / sampleSize) >= Settings.MAX_DIMENSION_PX) {
                sampleSize *= 2
            }

            return sampleSize
        }
    }

    private data class BitmapSize(
        val width: Int,
        val height: Int
    )
}
