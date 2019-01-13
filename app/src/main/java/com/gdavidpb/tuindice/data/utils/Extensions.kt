package com.gdavidpb.tuindice.data.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.PowerManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.CycleInterpolator
import android.widget.EditText
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.gdavidpb.tuindice.data.model.Validation
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import kotlinx.coroutines.*
import okhttp3.RequestBody
import okio.Buffer
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import java.io.InputStream
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/* Rx Java */

fun <T> Maybe<T>.andThen(completable: Completable): Maybe<T> = flatMap { completable.andThen(Maybe.just(it)) }

fun <T> Completable.asSingle(value: T): Single<T> = andThen(Single.just(value))

fun <T> Flowable<T>.firstOrError(predicate: (T) -> Boolean): Single<T> = filter { predicate(it) }.firstOrError()

/* Validation */

fun Array<Validation>.setUp() {
    forEach { set ->
        with(set.view) {
            editText.notNull { editText ->
                editText.textChangedListener {
                    afterTextChanged { error = null }
                }
            }
        }
    }
}

fun Array<Validation>.firstInvalid(onFound: (TextInputLayout.() -> Unit)? = null) = firstOrNull { set ->
    set.run {
        validator(view).also { invalid ->
            if (invalid) {
                with(view) {
                    error = context.getString(set.resource)
                    onFound.notNull { it.invoke(this) }
                }
            }
        }
    }
}

infix fun TextInputLayout.set(@StringRes resource: Int) = this to resource

infix fun Pair<TextInputLayout, Int>.`when`(valid: TextInputLayout.() -> Boolean) = Validation(first, second, valid)

/* Destructuring collection */

operator fun <E> Collection<E>.component6(): E = elementAt(5)
operator fun <E> Collection<E>.component7(): E = elementAt(6)
operator fun <E> Collection<E>.component8(): E = elementAt(7)

fun RequestBody?.bodyToString(): String {
    if (this == null) return ""

    val buffer = Buffer().also(::writeTo)

    val string = buffer.readUtf8()

    buffer.close()

    return string
}

/* View */

fun View.onClickOnce(onClick: (View) -> Unit) {
    setOnClickListener(object : View.OnClickListener {
        override fun onClick(view: View) {
            view.setOnClickListener(null)

            also { listener ->
                GlobalScope.launch {
                    withContext((Dispatchers.Main)) {
                        onClick(view)
                    }

                    delay(TIME_DELAY_CLICK_ONCE)

                    view.setOnClickListener(listener)
                }
            }
        }
    })
}

fun TextInputLayout.selectAll() = editText?.selectAll()

fun EditText.drawables(
        left: Drawable? = null,
        top: Drawable? = null,
        right: Drawable? = null,
        bottom: Drawable? = null) = setCompoundDrawables(left, top, right, bottom)

fun EditText.onTextChanged(event: (CharSequence, Int, Int, Int) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            event(s ?: "", start, before, count)
        }

        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    })
}

fun TextInputLayout.text(value: String? = null) = value?.also { editText?.setText(it) }
        ?: "${editText?.text}"

/* Preferences */

fun SharedPreferences.edit(transaction: SharedPreferences.Editor.() -> Unit) {
    with(edit()) {
        transaction()
        apply()
    }
}

/* Context */

fun Context.getCompatColor(@ColorRes colorRes: Int): Int = ContextCompat.getColor(this, colorRes)

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

fun Context.isPowerSaveMode(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        powerManager.isPowerSaveMode
    } else
        false
}

/* System services */

fun ConnectivityManager.isNetworkAvailable() = activeNetworkInfo?.isConnected == true

/* Format */

fun Double.toGrade() = Math.floor(this * 10000) / 10000

fun Date.format(format: String): String? = SimpleDateFormat(format, DEFAULT_LOCALE).runCatching { format(this@format) }.getOrNull()

fun String.parse(format: String): Date? = SimpleDateFormat(format, DEFAULT_LOCALE).runCatching { parse(this@parse) }.getOrNull()

fun String.toShortName(): String {
    val array = split("\\s+".toRegex())

    return when {
        array.size == 1 -> arrayOf(0)
        array.size == 2 -> arrayOf(0, 1)
        array.size >= 3 -> arrayOf(0, 2)
        else -> emptyArray()
    }.run {
        joinToString(" ") { array[it] }
    }
}

/* Utils */

fun X509Certificate.getProperty(key: String) = "(?<=$key=)[^,]+|$".toRegex().find(subjectDN.name)?.value

fun ZipOutputStream.putEntry(entry: ZipEntry, inputStream: InputStream) {
    inputStream.use {
        putNextEntry(entry)

        it.copyTo(this)

        closeEntry()

        flush()
    }
}

inline fun Any?.isNull(exec: () -> Unit) = this ?: exec()

inline fun <T> T?.notNull(exec: (T) -> Unit): T? = this?.also { exec(this) }

/* Animation */

fun ValueAnimator.animate(
        init: ValueAnimator.() -> Unit,
        update: ValueAnimator.() -> Unit,
        finish: ValueAnimator.(Boolean) -> Unit = { }) {
    init()

    addUpdateListener(update)

    addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            animation.removeAllListeners()

            finish(false)
        }

        override fun onAnimationCancel(animation: Animator) {
            animation.removeAllListeners()

            finish(true)
        }
    })

    start()
}

fun View.lookAtMe(factor: Float = 3f) {
    ValueAnimator.ofFloat(-factor, factor).animate({
        duration = (factor * 100).toLong()
        interpolator = CycleInterpolator(factor)
    }, {
        translationX = animatedValue as Float
    })
}

fun <T : View> T.updateAtMe(update: T.() -> Unit) {
    if (context.isPowerSaveMode()) {
        update()
        return
    }

    val animator = ValueAnimator.ofFloat(1f, 0f)

    animator.animate({
        duration = 250
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