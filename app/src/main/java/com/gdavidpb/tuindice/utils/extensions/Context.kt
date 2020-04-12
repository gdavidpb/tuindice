package com.gdavidpb.tuindice.utils.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.DrawableCompat
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.ui.activities.BrowserActivity
import com.gdavidpb.tuindice.utils.EXTRA_TITLE
import com.gdavidpb.tuindice.utils.EXTRA_URL
import com.gdavidpb.tuindice.utils.PACKAGE_NAME_WEB_VIEW
import com.gdavidpb.tuindice.utils.ResourcesManager
import com.gdavidpb.tuindice.utils.mappers.fillIntentArguments
import com.gdavidpb.tuindice.utils.mappers.runCatchingIsSuccess
import java.io.File

fun Context.openSettings() {
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let(::startActivity)
}

fun Context.openPdf(file: File) {
    FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, file).also { uri ->
        grantUriPermission("com.google.android.apps.docs", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }.also { uri ->
        Intent(Intent.ACTION_VIEW, uri)
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .let(::startActivity)
    }
}

fun Context.share(text: String, subject: String = "") = runCatchingIsSuccess {
    Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }.also { intent ->
        startActivity(Intent.createChooser(intent, null))
    }
}

fun Context.email(email: String, subject: String = "", text: String = "") = runCatchingIsSuccess {
    Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))

        if (subject.isNotEmpty()) putExtra(Intent.EXTRA_SUBJECT, subject)
        if (text.isNotEmpty()) putExtra(Intent.EXTRA_TEXT, text)
    }.also(::startActivity)
}

fun Context.browse(url: String) = runCatchingIsSuccess {
    Intent(Intent.ACTION_VIEW, Uri.parse(url)).also(::startActivity)
}

fun Context.versionName(): String {
    val environmentRes = if (BuildConfig.DEBUG) R.string.debug else R.string.release

    return getString(R.string.app_version,
            getString(environmentRes),
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE)
}

inline fun <reified T : Activity> Context.startActivity(vararg params: Pair<String, Any?>) = runCatchingIsSuccess {
    Intent(this, T::class.java).apply {
        if (params.isNotEmpty()) fillIntentArguments(params)
    }.also(::startActivity)
}

fun Context.browserActivity(@StringRes title: Int, url: String) {
    if (isPackageInstalled(PACKAGE_NAME_WEB_VIEW))
        startActivity<BrowserActivity>(EXTRA_TITLE to getString(title), EXTRA_URL to url)
    else
        browse(url)
}

fun Context.getCompatColor(@ColorRes colorRes: Int): Int = ContextCompat.getColor(this, colorRes)

fun Context.getCompatVector(@DrawableRes drawableRes: Int, width: Int, height: Int): Drawable {
    return DrawableCompat.wrap(AppCompatResources.getDrawable(this, drawableRes)!!).apply {
        bounds = Rect(0, 0, width, height)
    }
}

fun Context.getCompatDrawable(@DrawableRes resId: Int): Drawable = AppCompatResources.getDrawable(this, resId)!!

fun Context.getCompatDrawable(@DrawableRes drawableRes: Int, @ColorRes colorRes: Int): Drawable {
    val color = ResourcesManager.getColor(colorRes, this)
    val drawable = DrawableCompat.wrap(getCompatDrawable(drawableRes))

    return drawable.apply {
        DrawableCompat.setTint(this, color)
        DrawableCompat.setTintMode(this, PorterDuff.Mode.MULTIPLY)

        bounds = Rect(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    }
}

fun Context.isPowerSaveMode() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

    powerManager.isPowerSaveMode
} else
    false

fun Context.isPackageInstalled(packageName: String) = runCatching {
    packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
}.isSuccess