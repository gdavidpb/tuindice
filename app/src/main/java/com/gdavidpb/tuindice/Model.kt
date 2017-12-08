package com.gdavidpb.tuindice

import android.content.Context
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

enum class Operation(val value: Int,
                     private @StringRes val stringRes: Int) {
    CONNECT(0, R.string.messageConnecting),
    LOGIN_RECORD(1, R.string.messageLogging),
    COLLECT_RECORD(2, R.string.messageCollecting),
    COLLECT_PERSONAL(3, R.string.messagePersonal),
    LOGIN_ENROLLMENT(4, R.string.messageThisLogin),
    COLLECT_ENROLLMENT(5, R.string.messageThis),
    FINISH(6, R.string.messageFinishing);

    fun toString(context: Context): String = context.getString(stringRes)
}

enum class SubjectStatus(val value: Int, private @StringRes val stringRes: Int) {
    NONE(-1, 0),
    OK(0, 0),
    RETIRED(1, R.string.retired),
    NO_EFFECT(2, R.string.noEffect),
    APPROVED(3, R.string.approved);

    fun toString(context: Context): String = context.getString(stringRes)

    companion object {
        fun fromValue(value: Int): SubjectStatus = when (value) {
            OK.value -> OK
            RETIRED.value -> RETIRED
            NO_EFFECT.value -> NO_EFFECT
            APPROVED.value -> APPROVED
            else -> NONE
        }
    }
}

enum class QuarterType(val value: Int, private @ColorRes val color: Int) {
    NONE(-1, 0),
    ALL(0, 0),
    CURRENT(1, R.color.quarterCurrent),
    COMPLETED(2, R.color.quarterCompleted),
    GUESS(3, R.color.quarterGuess);

    fun getColor(context: Context) = ContextCompat.getColor(context, color)

    companion object {
        fun fromValue(value: Int): QuarterType = when (value) {
            ALL.value -> ALL
            CURRENT.value -> CURRENT
            COMPLETED.value -> COMPLETED
            GUESS.value -> GUESS
            else -> NONE
        }
    }
}

data class DstResponse<out T: Serializable>(val result: T? = null,
                              var exception: Exception? = null): Serializable {
    constructor(exception: Exception) : this(null, exception)
}

data class DstAccount(val usbId: String = String(),
                      val password: String = String()): Serializable {

    var id: Long = -1

    var firstNames = String()
    var lastNames = String()
    var careerName = String()

    var careerCode: Int = 0
    var lastUpdate: Long = 0
    var failedCredits: Int = 0
    var failedSubjects: Int = 0
    var retiredCredits: Int = 0
    var retiredSubjects: Int = 0
    var approvedCredits: Int = 0
    var approvedSubject: Int = 0
    var enrolledCredits: Int = 0
    var enrolledSubjects: Int = 0

    val quarters = ArrayList<DstQuarter>()

    constructor(usbId: String,
                password: String,
                firstNames: String,
                lastNames: String,
                careerName: String,
                careerCode: Int,
                lastUpdate: Long,
                failedCredits: Int,
                failedSubjects: Int,
                retiredCredits: Int,
                retiredSubjects: Int,
                approvedCredits: Int,
                approvedSubject: Int,
                enrolledCredits: Int,
                enrolledSubjects: Int) : this(usbId, password) {
        this.firstNames = firstNames
        this.lastNames = lastNames
        this.careerName = careerName
        this.careerCode = careerCode
        this.lastUpdate = lastUpdate
        this.failedCredits = failedCredits
        this.failedSubjects = failedSubjects
        this.retiredCredits = retiredCredits
        this.retiredSubjects = retiredSubjects
        this.approvedCredits = approvedCredits
        this.approvedSubject = approvedSubject
        this.enrolledCredits = enrolledCredits
        this.enrolledSubjects = enrolledSubjects
    }

    fun isOutdated() = (id == -1L)

    fun isEmpty() = usbId.isEmpty() || password.isEmpty()

    override fun equals(other: Any?): Boolean {
        if (other == this) return true
        if (other == null) return false

        return if (other is DstAccount)
            other.usbId.compareTo(this.usbId) == 0
        else
            false
    }

    override fun hashCode() = id.toInt()
}

data class DstSubject(val code: String,
                      val name: String,
                      val credits: Int,
                      var grade: Int = 3,
                      var status: SubjectStatus = SubjectStatus.OK): Serializable {

    var id: Long = 0

    override fun equals(other: Any?): Boolean {
        if (other == this) return true
        if (other == null) return false

        return if (other is DstSubject)
            other.code.compareTo(this.code) == 0
        else
            false
    }

    override fun hashCode() = id.toInt()
}

data class DstQuarter(val type: QuarterType,
                      val startTime: Long,
                      val endTime: Long,
                      var grade: Double = 0.0,
                      var gradeSum: Double = 0.0): Serializable {

    var id: Long = 0
    var aid: Long = 0

    val subjects = ArrayList<DstSubject>()

    private val monthFormat = SimpleDateFormat("MMM", Constants.DEFAULT_LOCALE)
    private val yearFormat = SimpleDateFormat("yyyy", Constants.DEFAULT_LOCALE)

    fun isOk(credits: Int): Boolean = if (credits == 0 || credits >= 8 || (type == QuarterType.COMPLETED))
        true
    else {
        val isSummer: Boolean
        val start = Calendar.getInstance()
        val end = Calendar.getInstance()

        start.timeInMillis = startTime
        end.timeInMillis = endTime

        isSummer = (start.monthsTo(end) != 3)

        isSummer
    }

    fun getName(): String {
        val start = Date(startTime)
        val end = Date(endTime)
        val builder = StringBuilder()

        builder.append(monthFormat.format(start).capitalize())
        builder.append(" - ")
        builder.append(monthFormat.format(end).capitalize())
        builder.append(" ")
        builder.append(yearFormat.format(start))

        return builder.toString()
    }

    fun getCacheKey(): Long {
        val base = Math.pow(10.0, subjects.size.toDouble()).toLong()

        return (base + subjects.indices.sumBy { (Math.pow(10.0, it.toDouble()) * subjects[it].grade).toInt() })
    }

    override fun equals(other: Any?): Boolean {
        if (other == this) return true
        if (other == null) return false

        return if (other is DstQuarter)
            other.startTime == this.startTime &&
                    other.endTime == this.endTime
        else
            false
    }

    override fun hashCode() = id.toInt()
}