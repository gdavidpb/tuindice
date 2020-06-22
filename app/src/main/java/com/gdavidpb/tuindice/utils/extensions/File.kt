package com.gdavidpb.tuindice.utils.extensions

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.utils.DEFAULT_SHRED_PASSES
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom

fun Uri.fileProviderUri(context: Context): Uri =
        FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, toFile())

fun File.shred() {
    val random = SecureRandom()
    val length = length()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

    repeat(DEFAULT_SHRED_PASSES) {
        FileOutputStream(this).use { stream ->
            do {
                random.nextBytes(buffer)

                stream.write(buffer, 0, buffer.size)
            } while (stream.channel.position() < length)

            stream.flush()
        }
    }

    delete()
}