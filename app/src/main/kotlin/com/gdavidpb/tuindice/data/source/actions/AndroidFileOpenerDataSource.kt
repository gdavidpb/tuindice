package com.gdavidpb.tuindice.data.source.actions

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.gdavidpb.tuindice.base.domain.repository.FileOpenerRepository
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import java.io.File

class AndroidFileOpenerDataSource(
	private val context: Context
) : FileOpenerRepository {
	override fun openFile(file: PlatformFile): Boolean {
		val source = file.path
		val uri = source.toUri()

		return if (uri.scheme == ContentResolver.SCHEME_CONTENT || uri.scheme == ContentResolver.SCHEME_FILE) {
			openUri(uri = uri, mimeType = context.contentResolver.getType(uri))
		} else {
			val file = File(source)
			val uri = FileProvider.getUriForFile(context, context.packageName, file)
			val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)

			openUri(uri, mimeType)
		}
	}

	private fun openUri(uri: Uri, mimeType: String?): Boolean {
		return runCatching {
			val intent = Intent(Intent.ACTION_VIEW).apply {
				setDataAndType(uri, mimeType)
				addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
			}

			context.startActivity(intent)
		}.isSuccess
	}
}
