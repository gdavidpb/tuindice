package com.gdavidpb.tuindice.summary.utils.extension

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun File.fileProviderUri(context: Context): Uri =
	FileProvider.getUriForFile(context, context.packageName, this)