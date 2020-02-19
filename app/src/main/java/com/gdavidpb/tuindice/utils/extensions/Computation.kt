package com.gdavidpb.tuindice.utils.extensions

import android.util.Base64
import com.gdavidpb.tuindice.data.model.database.QuarterEntity
import com.gdavidpb.tuindice.data.model.database.SubjectEntity
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.presentation.model.QuarterItem
import com.gdavidpb.tuindice.utils.DigestConcat
import com.gdavidpb.tuindice.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.utils.STATUS_QUARTER_RETIRED
import com.gdavidpb.tuindice.utils.STATUS_SUBJECT_RETIRED
import java.nio.ByteBuffer
import java.util.*

fun Int.toGrade() = when (this) {
    in 0 until 30 -> 1
    in 30 until 50 -> 2
    in 50 until 70 -> 3
    in 70 until 85 -> 4
    else -> MAX_SUBJECT_GRADE
}

fun QuarterItem.computeGrade() = data.subjects.computeGrade()

fun QuarterItem.computeCredits() = data.subjects.computeCredits()

fun Subject.isApproved() = grade >= 3

fun Collection<Subject>.computeGrade(): Double {
    val creditsSum = computeCredits().toDouble()

    val weightedSum = sumBy {
        it.grade * it.credits
    }.toDouble()

    return if (creditsSum != 0.0) weightedSum / creditsSum else 0.0
}

fun Collection<Subject>.computeCredits() = sumBy {
    if (it.grade != 0) it.credits else 0
}

fun String.isUsbId() = matches("^\\d{2}-\\d{5}$".toRegex())

fun Account.isUpdated(): Boolean {
    val now = Date()
    val outdated = lastUpdate.tomorrow()

    return now.before(outdated)
}

private val gradeSumCache = hashMapOf<Int, Double>()

private fun Collection<QuarterItem>.internalComputeGradeSum(until: QuarterItem) =
        /* Until quarter and not retired */
        filter { it.data.startDate <= until.data.startDate && it.data.status != STATUS_QUARTER_RETIRED }
                /* Get all subjects */
                .flatMap { it.data.subjects }
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

fun Collection<QuarterItem>.computeGradeSum(until: QuarterItem) =
        gradeSumCache.getOrPut(until.hashCode()) { internalComputeGradeSum(until) }

private val digestConcat = DigestConcat(algorithm = "SHA-256")

fun QuarterEntity.generateId(): String {
    val hash = digestConcat
            .concat(data = userId)
            .concat(data = startDate.seconds)
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
            .replace("[/+=\n]+".toRegex(), "")
            .substring(0..userId.length)
}

fun Long.bytes(): ByteArray = ByteBuffer.allocate(Long.SIZE_BYTES).run {
    putLong(this@bytes).array().also { clear() }
}