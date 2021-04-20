package com.gdavidpb.tuindice.utils.extensions

import android.content.Context
import android.content.SharedPreferences
import android.graphics.PorterDuff
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
fun Context.getCompatColor(@ColorRes colorRes: Int): Int = ResourcesManager.getColor(colorRes, this)

fun Context.getCompatVector(@DrawableRes drawableRes: Int, width: Int, height: Int): Drawable {
    return DrawableCompat.wrap(AppCompatResources.getDrawable(this, drawableRes)!!).apply {
        bounds = Rect(0, 0, width, height)
    }
}

fun Context.getCompatDrawable(@DrawableRes resId: Int): Drawable = AppCompatResources.getDrawable(this, resId)!!

fun Context.getCompatDrawable(@DrawableRes drawableRes: Int, @ColorRes colorRes: Int): Drawable {
    val color = getCompatColor(colorRes)
    val drawable = DrawableCompat.wrap(getCompatDrawable(drawableRes))

    return drawable.apply {
        DrawableCompat.setTint(this, color)
        DrawableCompat.setTintMode(this, PorterDuff.Mode.MULTIPLY)

        bounds = Rect(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
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