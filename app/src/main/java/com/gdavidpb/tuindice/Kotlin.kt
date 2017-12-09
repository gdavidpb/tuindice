package com.gdavidpb.tuindice

import android.accounts.AuthenticatorException
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.content.res.AppCompatResources
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.CycleInterpolator
import com.gdavidpb.tuindice.models.database
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.alert
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import java.io.*
import java.net.SocketTimeoutException
import java.util.*
import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.net.ssl.SSLException

/* This document contains some possessed functions: In Kotlin we trust */

/* So powerful dark magic */
fun <T> async(task: () -> T, onFinish: suspend T.() -> Unit = { }) {
    async(UI) { bg { task() }.await().let { response -> onFinish(response) } }
}

/* ZipOutputStream extension */
fun ZipOutputStream.putEntry(entry: ZipEntry, inputStream: InputStream) {
    inputStream.use {
        putNextEntry(entry)

        inputStream.copyTo(this)

        closeEntry()

        flush()
    }
}

/* Exception extension */
fun Exception.toDescription(): Int = when (this) {
    is AuthenticatorException -> R.string.snackAuthError
    is SocketTimeoutException -> R.string.snackTimeoutError
    is SSLException -> R.string.snackSSLError
    is FileNotFoundException -> R.string.snackServerError
    is IllegalStateException -> R.string.snackParseError
    else -> R.string.snackNetError
}

/* String extensions */
fun String.toSubjectName(): String {
    val buffer = StringBuffer()

    val matcher = Pattern
            .compile("(?<=\\b)[xvi]+(?<=\\b|\$)|(?<=^|\\W)\\w")
            .matcher(toLowerCase()
                    .replace("^\"|\"$".toRegex(), "")
                    .replace("(?<=\\w)\\.(?=\\w)".toRegex(), " "))

    while (matcher.find())
        matcher.appendReplacement(buffer, matcher.group().toUpperCase())

    matcher.appendTail(buffer)

    return buffer.toString()
}

fun String.toPeriodName() = "\\S+-\\S+ \\d{4}"
        .toRegex()
        .find(replace("\\s*-\\s*".toRegex(), "-").toLowerCase())?.value

fun Element.unescapeEntitiesText() = Parser.unescapeEntities(text(), false).trim()

/* Double extensions */
fun Double.toGrade() = Math.floor(this * 10000) / 10000

/* Calendar extensions */
fun Calendar.monthsTo(to: Calendar): Int {
    return (Math.abs(to.get(Calendar.YEAR) - get(Calendar.YEAR)) * 12) +
            Math.abs(to.get(Calendar.MONTH) - get(Calendar.MONTH))
}

/* Context extensions */
fun Context.logReport(operation: Operation, document: Document, removeSensitive: Document.() -> Unit = { }) {
    /* Document clone */
    val documentClone = document.clone()

    /* Remove sensible data from url */
    val url = documentClone.location().replace("([;?]).*".toRegex(), "")

    /* Remove sensible elements */
    documentClone.removeSensitive()

    File(filesDir, "report-operation").appendText("$operation - $url\n\n$documentClone\n\n")
}

fun Context.deleteReport() {
    for (f in filesDir.listFiles())
        try {
            f.delete()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
}

fun Context.isPowerSaveMode(): Boolean {
    return if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        powerManager.isPowerSaveMode
    } else
        false
}

/* Activity extension for contact */
fun Activity.onContact(showAlert: Boolean = true) {
    val startContact = {
        val intent = Intent(Intent.ACTION_SENDTO)
                .setData(Uri.parse("mailto:"))
                .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.devContactSubject))
                .putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.devEmail)))

        val report = database.generateReport(applicationContext)

        if (report != null)
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(report))
        else
            longToast(R.string.devUnableGenerateReport)

        try {
            startActivity(intent)
        } catch (exception: ActivityNotFoundException) {
            exception.printStackTrace()

            toast(R.string.devNoEmailClient)
        }
    }

    if (showAlert)
        alert(R.string.devMessageReport,
                R.string.devTitleReport) {
            negativeButton(R.string.cancel) { }
            positiveButton(R.string.accept) { startContact() }
        }.show()
    else
        startContact()
}

fun Cursor.moveRandom() = if (count > 0) move(Math.abs(Random().nextInt()) % count + 1) else false

/* Default animations extensions */
fun View.lookAtMe(factor: Float = 3f) {
    ValueAnimator.ofFloat(-factor, factor).animate({
        duration = (factor * 100).toLong()
        repeatCount = 1
        interpolator = CycleInterpolator(factor)
    }, {
        translationX = animatedValue as Float
    })
}

fun <T: View> T.updateAtMe(update: T.() -> Unit) {
    if (context.isPowerSaveMode()) {
        update()
        return
    }

    val animator = ValueAnimator.ofFloat(1f, 0f)

    animator.animate({
        duration = 250
        repeatCount = 1
        repeatMode = ValueAnimator.REVERSE
        interpolator = AccelerateDecelerateInterpolator()
    }, {
        alpha = animatedValue as Float
    })

    animator.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationRepeat(animation: Animator) {
            update()
        }
    })
}

fun View.demoAtMe(): ValueAnimator {
    val animator = ValueAnimator.ofFloat(1f, 1.25f)

    animator.animate({
        duration = 500
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
        interpolator = AccelerateDecelerateInterpolator()
    }, {
        scaleX = animatedValue as Float
        scaleY = animatedValue as Float
    })

    return animator
}

/* Value animator extension */
fun ValueAnimator.animate(
        init: ValueAnimator.() -> Unit,
        animation: ValueAnimator.() -> Unit) {
    init()

    addUpdateListener(animation)

    addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animator: Animator) {
            animator.removeAllListeners()
        }
    })

    start()
}

/* Resource drawables extensions */
fun Context.getTintVector(
        @DrawableRes drawableRes: Int,
        @ColorRes colorRes: Int): Drawable {
    val color = ContextCompat.getColor(this, colorRes)
    val drawable = DrawableCompat.wrap(AppCompatResources.getDrawable(this, drawableRes)!!)

    DrawableCompat.setTint(drawable, color)
    DrawableCompat.setTintMode(drawable, PorterDuff.Mode.MULTIPLY)

    drawable.bounds = Rect(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)

    return drawable
}