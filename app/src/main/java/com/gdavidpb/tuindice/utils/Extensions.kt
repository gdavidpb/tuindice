package com.gdavidpb.tuindice.utils

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.gdavidpb.tuindice.data.model.app.Validation
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.coroutines.Continuous
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
import okhttp3.RequestBody
import okio.Buffer
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import java.io.InputStream
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/* Live data */

typealias LiveResult<T> = MutableLiveData<Result<T>>
typealias LiveCompletable = MutableLiveData<Completable>
typealias LiveContinuous<T> = MutableLiveData<Continuous<T>>

/* LiveResult */

@JvmName("postCompleteResult")
fun <T> LiveResult<T>.postSuccess(value: T) = postValue(Result.OnSuccess(value))

@JvmName("postThrowableResult")
fun <T> LiveResult<T>.postThrowable(throwable: Throwable) = postValue(Result.OnError(throwable))

@JvmName("postLoadingResult")
fun <T> LiveResult<T>.postLoading() = postValue(Result.OnLoading())

@JvmName("postCancelResult")
fun <T> LiveResult<T>.postCancel() = postValue(Result.OnCancel())

@JvmName("postEmptyResult")
fun <T> LiveResult<T>.postEmpty() = postValue(Result.OnEmpty())

/* LiveCompletable */

@JvmName("postCompleteCompletable")
fun LiveCompletable.postComplete() = postValue(Completable.OnComplete)

@JvmName("postThrowableCompletable")
fun LiveCompletable.postThrowable(throwable: Throwable) = postValue(Completable.OnError(throwable))

@JvmName("postLoadingCompletable")
fun LiveCompletable.postLoading() = postValue(Completable.OnLoading)

@JvmName("postCancelCompletable")
fun LiveCompletable.postCancel() = postValue(Completable.OnCancel)

/* LiveContinuous */

@JvmName("postNextContinuous")
fun <T> LiveContinuous<T>.postNext(value: T) = postValue(Continuous.OnNext(value))

@JvmName("postCompleteContinuous")
fun <T> LiveContinuous<T>.postComplete() = postValue(Continuous.OnComplete())

@JvmName("postThrowableContinuous")
fun <T> LiveContinuous<T>.postThrowable(throwable: Throwable) = postValue(Continuous.OnError(throwable))

@JvmName("postCancelContinuous")
fun <T> LiveContinuous<T>.postCancel() = postValue(Continuous.OnCancel())

fun <T, L : LiveData<T>> FragmentActivity.observe(liveData: L, body: (T?) -> Unit) =
        liveData.observe(this, Observer(body))

fun <T, L : LiveData<T>> Fragment.observe(liveData: L, body: (T?) -> Unit) =
        liveData.observe(viewLifecycleOwner, Observer(body))

/* Coroutines */

@JvmName("awaitVoid")
suspend fun Task<Void>.await() = suspendCoroutine<Unit> { continuation ->
    addOnSuccessListener { continuation.resume(Unit) }
    addOnFailureListener { continuation.resumeWithException(it) }
}

@JvmName("awaitTResult")
suspend fun <TResult> Task<TResult>.await() = suspendCoroutine<TResult> { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
}

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

fun View.onClickOnce(onClick: () -> Unit) {
    setOnClickListener(object : View.OnClickListener {
        override fun onClick(view: View) {
            view.setOnClickListener(null)

            also { listener ->
                CoroutineScope(Dispatchers.Main).launch {
                    onClick()

                    withContext(Dispatchers.IO) { delay(TIME_DELAY_CLICK_ONCE) }

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

fun Long.toCountdown(): String {
    val min = TimeUnit.MILLISECONDS.toMinutes(this)
    val sec = TimeUnit.MILLISECONDS.toSeconds(this - TimeUnit.MINUTES.toMillis(min))

    return String.format("%02d:%02d", min, sec)
}

fun Double.toGrade() = Math.floor(this * 10000) / 10000

fun Double.formatGrade() = String.format("%.4f", this)

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