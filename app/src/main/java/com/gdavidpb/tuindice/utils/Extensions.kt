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
import android.util.Base64
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.CycleInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.Guideline
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.gdavidpb.tuindice.data.model.database.QuarterEntity
import com.gdavidpb.tuindice.data.model.database.SubjectEntity
import com.gdavidpb.tuindice.data.utils.Validation
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.coroutines.Continuous
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.ui.activities.BrowserActivity
import com.google.android.gms.tasks.Task
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
import okhttp3.RequestBody
import okio.Buffer
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import org.jetbrains.anko.startActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.nio.ByteBuffer
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.absoluteValue
import kotlin.math.floor

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

@JvmName("postStartContinuous")
fun <T> LiveContinuous<T>.postStart() = postValue(Continuous.OnStart())

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
suspend fun <T> Task<T>.await() = suspendCoroutine<T> { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
}

suspend fun <T> Call<T>.await() = suspendCoroutine<T?> { continuation ->
    enqueue(object : Callback<T?> {
        override fun onResponse(call: Call<T?>, response: Response<T?>) {
            if (response.isSuccessful)
                continuation.resume(response.body())
            else
                continuation.resumeWithException(HttpException(response))
        }

        override fun onFailure(call: Call<T?>, t: Throwable) {
            continuation.resumeWithException(t)
        }
    })
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

fun SeekBar.onSeekBarChange(listener: (progress: Int, fromUser: Boolean) -> Unit) {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            listener(progress, fromUser)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    })
}

fun AppBarLayout.onStateChanged(listener: (newState: Int) -> Unit) {
    var mCurrentState = STATE_IDLE

    addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
        when {
            verticalOffset == 0 -> {
                if (mCurrentState != STATE_EXPANDED)
                    listener(STATE_EXPANDED)

                mCurrentState = STATE_EXPANDED
            }
            verticalOffset.absoluteValue >= appBarLayout.totalScrollRange -> {
                if (mCurrentState != STATE_COLLAPSED)
                    listener(STATE_COLLAPSED)

                mCurrentState = STATE_COLLAPSED
            }
            else -> {
                if (mCurrentState != STATE_IDLE)
                    listener(STATE_IDLE)

                mCurrentState = STATE_IDLE
            }
        }
    })
}

fun RecyclerView.onScrollStateChanged(listener: (newState: Int) -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            listener(newState)
        }
    })
}

fun TextInputLayout.selectAll() = editText?.selectAll()

fun EditText.drawables(
        left: Drawable? = null,
        top: Drawable? = null,
        right: Drawable? = null,
        bottom: Drawable? = null) = setCompoundDrawables(left, top, right, bottom)

fun TextView.drawables(
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

fun Context.browserActivity(@StringRes title: Int, url: String) {
    startActivity<BrowserActivity>(EXTRA_TITLE to getString(title), EXTRA_URL to url)
}

fun Context.getCompatColor(@ColorRes colorRes: Int): Int = ContextCompat.getColor(this, colorRes)

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

fun Date.formatLastUpdate(): String {
    val now = Date()
    val diff = now.time - time
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        time == 0L -> "Nunca"
        isToday() -> format("'Hoy,' hh:mm aa")
        isYesterday() -> format("'Ayer,' hh:mm aa")
        days < 7 -> format("EEEE',' hh:mm aa")
        else -> format("dd 'de' MMMM yyyy")
    }?.capitalize() ?: "-"
}

fun Long.toCountdown(): String {
    val min = TimeUnit.MILLISECONDS.toMinutes(this)
    val sec = TimeUnit.MILLISECONDS.toSeconds(this - TimeUnit.MINUTES.toMillis(min))

    return String.format("%02d:%02d", min, sec)
}

fun Double.toGrade() = floor(this * 10000) / 10000

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

/* Computation */

fun Quarter.computeGrade(): Double {
    return subjects.computeGrade()
}

fun Quarter.computeCredits(): Int {
    return subjects.computeCredits()
}

fun Subject.isApproved(): Boolean {
    return grade >= 3
}

fun Collection<Subject>.computeGrade(): Double {
    val creditsSum = computeCredits().toDouble()

    val weightedSum = sumBy {
        it.grade * it.credits
    }.toDouble()

    return if (creditsSum != 0.0) weightedSum / creditsSum else 0.0
}

fun Collection<Subject>.computeCredits(): Int {
    return sumBy {
        if (it.grade != 0) it.credits else 0
    }
}

private val gradeSumCache = hashMapOf<String, Double>()

private fun Collection<Quarter>.internalComputeGradeSum(until: Quarter): Double {
    /* Until quarter and not retired */
    return filter { it.startDate <= until.startDate && it.status != STATUS_QUARTER_RETIRED }
            /* Get all subjects */
            .flatMap { it.subjects }
            /* No take retired subjects */
            .filter { it.status != STATUS_SUBJECT_RETIRED }
            /* Group by code */
            .groupBy { it.code }
            .map { (_, subjects) ->
                /* If you've seen this subject more than once */
                if (subjects.size > 1)
                    subjects.toMutableList().also {
                        /* if last seen subject were approved, remove previous */
                        if (it.first().isApproved())
                            it.removeAt(1)
                    }
                else
                    subjects
            }.flatten()
            .computeGrade()
}

fun Collection<Quarter>.computeGradeSum(until: Quarter): Double {
    return gradeSumCache.getOrPut(until.id) { internalComputeGradeSum(until) }
}

/* Utils */

private val digestConcat = DigestConcat(algorithm = "SHA-256")

fun QuarterEntity.generateId(): String {
    val hash = digestConcat
            .concat(data = userId)
            .concat(data = startDate.toDate().format("MMMMyyyy")!!)
            .build()

    return Base64
            .encodeToString(hash, Base64.DEFAULT)
            .replace("[/+=\n]+".toRegex(), "")
            .substring(0..userId.length)
}

fun SubjectEntity.generateId(): String {
    val hash = digestConcat
            .concat(data = quarterId)
            .concat(data = code)
            .build()

    return Base64
            .encodeToString(hash, Base64.DEFAULT)
            .replace("[/+=]+".toRegex(), "")
            .substring(0..userId.length)
}

fun X509Certificate.getProperty(key: String) = "(?<=$key=)[^,]+|$".toRegex().find(subjectDN.name)?.value

fun Long.bytes(): ByteArray = ByteBuffer.allocate(Long.SIZE_BYTES)
        .putLong(this)
        .array()

fun Date.isToday(): Boolean {
    val start = Calendar.getInstance().run {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        Date(timeInMillis)
    }

    val end = Calendar.getInstance().run {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        add(Calendar.DATE, 1)
        add(Calendar.SECOND, -1)

        Date(timeInMillis)
    }

    return start.rangeTo(end).contains(this)
}

fun Date.isYesterday(): Boolean {
    val start = Calendar.getInstance().run {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DATE, -1)

        Date(timeInMillis)
    }

    val end = Calendar.getInstance().run {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        add(Calendar.SECOND, -1)

        Date(timeInMillis)

        Date(timeInMillis)
    }

    return start.rangeTo(end).contains(this)
}

fun Calendar.containsInMonth(value: Calendar): Boolean {
    val start = Calendar.getInstance().let {
        it.time = this.time

        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        set(Calendar.DAY_OF_MONTH, 1)

        Date(it.timeInMillis)
    }

    val end = Calendar.getInstance().let {
        it.time = start

        it.add(Calendar.MONTH, 1)

        Date(it.timeInMillis)
    }

    return start.rangeTo(end).contains(value.time)
}

infix fun Int.negRem(value: Int) = (this % value) + if (this >= 0) 0 else value

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

fun TextView.animateGrade(value: Double) {
    if (context.isPowerSaveMode()) {
        text = value.formatGrade()
        return
    }

    ValueAnimator.ofFloat(0f, value.toFloat()).animate({
        duration = 1000
        interpolator = DecelerateInterpolator()
    }, {
        text = (animatedValue as Float).toDouble().formatGrade()
    })
}

fun Guideline.animatePercent(value: Float) {
    if (context.isPowerSaveMode()) {
        setGuidelinePercent(value)
        return
    }

    ValueAnimator.ofFloat(0f, value).animate({
        duration = 1000
        interpolator = DecelerateInterpolator()
    }, {
        setGuidelinePercent(animatedValue as Float)
    })
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