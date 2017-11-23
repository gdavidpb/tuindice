package com.gdavidpb.tuindice

import android.accounts.AuthenticatorException
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.content.res.AppCompatResources
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.CycleInterpolator
import android.view.inputmethod.InputMethodManager
import com.gdavidpb.tuindice.models.Preferences
import com.gdavidpb.tuindice.models.SQLiteHelper
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.io.*
import java.net.SocketTimeoutException
import java.util.*
import javax.net.ssl.SSLException

/* This document contains some possessed functions: In Kotlin we trust */

/* So powerful dark magic */
fun <T> async(
        task: () -> T,
        onFinish: T.() -> Unit = { }) {

    launch(Android) {
        async(CommonPool) {
            task()
        }.await().let {
            response -> onFinish(response)
        }
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

/* Double extensions */
fun Double.toGrade() = Math.floor(this * 10000) / 10000

/* Calendar extensions */
fun Calendar.monthsTo(to: Calendar): Int {
    return (Math.abs(to.get(Calendar.YEAR) - get(Calendar.YEAR)) * 12) +
            Math.abs(to.get(Calendar.MONTH) - get(Calendar.MONTH))
}

/* Context extensions */
fun Context.getDatabase(): SQLiteHelper = SQLiteHelper.getInstance(this)

fun Context.getPreferences(): Preferences = Preferences.getInstance(this)

fun Context.isPowerSaveMode(): Boolean {
    return if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        powerManager.isPowerSaveMode
    } else
        false
}

inline fun Context.alertDialog(init: AlertDialog.Builder.() -> Unit) {
    val alertDialog = AlertDialog.Builder(this)

    alertDialog.init()

    alertDialog.show()
}

/* startActivity-launchActivity inline */
inline fun <reified T : Activity> Context.launchActivity(
        requestCode: Int = -1,
        options: Bundle? = null,
        noinline init: Intent.() -> Unit = { }) {

    val intent = Intent(this, T::class.java)

    intent.init()

    if (this is Activity && requestCode != -1)
        startActivityForResult(intent, requestCode, options)
    else
        startActivity(intent, options)
}

/* Database extensions */
fun SQLiteDatabase.rawQuery(sql: String): Cursor = rawQuery(sql, null)

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

/* View-Context inlines */
fun View.snackBar(
        @StringRes stringRes: Int,
        length: Int = Snackbar.LENGTH_LONG,
        init: Snackbar.() -> Unit = { }): Snackbar {
    val snackBar = Snackbar.make(this, stringRes, length)

    snackBar.init()

    snackBar.show()

    return snackBar
}

fun View.showSoftInput() {
    requestFocus()

    val inputManager = (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)

    inputManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun View.hideSoftInput() {
    clearFocus()

    val inputManager = (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)

    inputManager.hideSoftInputFromWindow(windowToken,
            InputMethodManager.HIDE_IMPLICIT_ONLY)

    inputManager.hideSoftInputFromWindow(windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS)
}