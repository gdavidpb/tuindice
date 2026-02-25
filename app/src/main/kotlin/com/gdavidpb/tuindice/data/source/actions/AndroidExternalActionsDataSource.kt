package com.gdavidpb.tuindice.data.source.actions

import android.content.Context
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.repository.ExternalActionsRepository
import java.io.File

class AndroidExternalActionsDataSource(
	private val context: Context
) : ExternalActionsRepository {
	override fun openFile(fileRef: PlatformFileRef): Boolean {
		val source = fileRef.value
		val uri = source.toUri()

		return if (uri.scheme == ContentResolver.SCHEME_CONTENT || uri.scheme == ContentResolver.SCHEME_FILE) {
			openUri(uri = uri, mimeType = context.contentResolver.getType(uri))
		} else {
			openFile(file = File(source))
		}
	}

	override fun sendEmail(email: String, subject: String, text: String) {
		runCatching {
			val intent = Intent(Intent.ACTION_SENDTO, "mailto:".toUri()).apply {
				putExtra(Intent.EXTRA_EMAIL, arrayOf(email))

				if (subject.isNotEmpty()) putExtra(Intent.EXTRA_SUBJECT, subject)
				if (text.isNotEmpty()) putExtra(Intent.EXTRA_TEXT, text)
			}

			context.startActivity(intent)
		}
	}

	override fun shareText(subject: String, text: String) {
		runCatching {
			val intent = Intent(Intent.ACTION_SEND).apply {
				type = "text/plain"
				putExtra(Intent.EXTRA_SUBJECT, subject)
				putExtra(Intent.EXTRA_TEXT, text)
			}.let {
				Intent.createChooser(it, null)
			}

			context.startActivity(intent)
		}
	}

	override fun openStorePage() {
		val packageName = context.packageName
		val intent = Intent(
			Intent.ACTION_VIEW,
			"market://details?id=$packageName".toUri()
		).apply {
			addFlags(
				Intent.FLAG_ACTIVITY_NO_HISTORY or
					Intent.FLAG_ACTIVITY_MULTIPLE_TASK or
					Intent.FLAG_ACTIVITY_NEW_DOCUMENT
			)
		}

		runCatching {
			context.startActivity(intent)
		}.onFailure {
			openBrowser("https://play.google.com/store/apps/details?id=$packageName")
		}
	}

	private fun openFile(file: File): Boolean {
		val uri = FileProvider.getUriForFile(context, context.packageName, file)
		val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)

		return openUri(uri, mimeType)
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

	private fun openBrowser(url: String) {
		runCatching {
			val intent = Intent(Intent.ACTION_VIEW, url.toUri())
			context.startActivity(intent)
		}
	}
}
