package com.gdavidpb.tuindice.summary.data.source

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.summary.domain.model.ShareableImage
import com.gdavidpb.tuindice.summary.domain.repository.ImageShareRepository
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Shares a PNG through the system share sheet. The image is written to the cache, where the app's
 * FileProvider serves the [IMAGE_SHARE_DIRECTORY_NAME] root to the receiving app, for reading only.
 */
class AndroidImageShareDataSource(
	private val context: Context,
	private val dispatchers: TuIndiceDispatchers
) : ImageShareRepository {
	override suspend fun shareImage(image: ShareableImage) {
		val file = withContext(dispatchers.io) { writeImage(image) }
		val uri = FileProvider.getUriForFile(context, context.packageName, file)

		withContext(dispatchers.main) {
			launchShareSheet(uri)
		}
	}

	// Replaces the previous image: the directory holds only the one being shared.
	internal fun writeImage(image: ShareableImage): File {
		val directory = File(context.cacheDir, IMAGE_SHARE_DIRECTORY_NAME)

		directory.deleteRecursively()
		directory.mkdirs()

		return File(directory, image.fileName).apply { writeBytes(image.content) }
	}

	internal fun launchShareSheet(uri: Uri) {
		val sendIntent = Intent(Intent.ACTION_SEND).apply {
			type = PNG_MIME_TYPE
			putExtra(Intent.EXTRA_STREAM, uri)
			clipData = ClipData.newRawUri(null, uri)
			addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
		}

		val chooserIntent = Intent.createChooser(sendIntent, null).apply {
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		}

		context.startActivity(chooserIntent)
	}

	private companion object {
		const val PNG_MIME_TYPE = "image/png"
	}
}
