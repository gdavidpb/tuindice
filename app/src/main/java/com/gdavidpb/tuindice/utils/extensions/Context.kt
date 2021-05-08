package com.gdavidpb.tuindice.utils.extensions

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.ResourcesManager

fun Context.versionName(): String {
    val environmentRes = if (BuildConfig.DEBUG) R.string.debug else R.string.release

    return getString(R.string.app_version,
            getString(environmentRes),
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE)
}

@ColorInt
fun Context.getCompatColor(@ColorRes colorRes: Int): Int {
    return ResourcesManager.getColor(colorRes, this)
}

fun Context.getCompatDrawable(
    @DrawableRes resId: Int,
    width: Int = -1,
    height: Int = -1
): Drawable {
    val drawable = AppCompatResources.getDrawable(this, resId) ?: error("resId: $resId")

    return if (width == -1 && height == -1) {
        drawable
    } else {
        DrawableCompat.wrap(drawable).apply {
            bounds = Rect(0, 0, width, height)
        }
    }
}

fun Context.sharedPreferences(): SharedPreferences {
    return PreferenceManager.getDefaultSharedPreferences(this)
}

@RequiresApi(api = Build.VERSION_CODES.M)
fun Context.provideMasterKey(): MasterKey {
    return MasterKey.Builder(this, BuildConfig.MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setRequestStrongBoxBacked(true)
            .build()
}

@Suppress("DEPRECATION")
fun Context.encryptedSharedPreferences(): SharedPreferences {
    val fileName = BuildConfig.APPLICATION_ID

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val masterKey = provideMasterKey()

        EncryptedSharedPreferences.create(
                this,
                fileName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } else {
        val masterKeyAlias = BuildConfig.MASTER_KEY_ALIAS

        EncryptedSharedPreferences.create(
                fileName,
                masterKeyAlias,
                this,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}