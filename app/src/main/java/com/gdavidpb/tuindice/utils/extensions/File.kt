package com.gdavidpb.tuindice.utils.extensions

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.gdavidpb.tuindice.BuildConfig

fun Uri.fileProviderUri(context: Context): Uri =
        FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, toFile())