package com.gdavidpb.tuindice.base.utils.extensions

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.gdavidpb.tuindice.base.BuildConfig

fun Uri.fileProviderUri(context: Context): Uri =
	FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, toFile())