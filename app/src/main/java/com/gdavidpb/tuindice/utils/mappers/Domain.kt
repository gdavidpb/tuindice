package com.gdavidpb.tuindice.utils.mappers

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.*
import com.gdavidpb.tuindice.domain.model.service.*
import com.gdavidpb.tuindice.utils.DigestConcat
import com.gdavidpb.tuindice.utils.MAX_QUARTER_GRADE
import com.gdavidpb.tuindice.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.utils.STATUS_QUARTER_CURRENT
import com.gdavidpb.tuindice.utils.extensions.base64
import com.gdavidpb.tuindice.utils.extensions.computeCredits
import java.util.*

private val digestConcat = DigestConcat(algorithm = "SHA-256")

fun generateQuarterId(uid: String, startDate: Date, endDate: Date) = digestConcat
        .concat(data = uid)
        .concat(data = (startDate to endDate).formatQuarterTitle())
        .build()
        .base64()
        .replace("[/+=\n]+".toRegex(), "")
        .substring(uid.indices)

fun generateSubjectId(uid: String, qid: String, code: String) = digestConcat
        .concat(data = qid)
        .concat(data = code)
        .build()
        .base64()
        .replace("[/+=\n]+".toRegex(), "")
        .substring(uid.indices)

fun buildAccount(auth: Auth, sigIn: DstAuth, personal: DstPersonal, record: DstRecord) = Account(
        uid = auth.uid,
        id = personal.id,
        usbId = personal.usbId,
        email = auth.email,
        fullName = sigIn.fullName,
        firstNames = personal.firstNames,
        lastNames = personal.lastNames,
        careerName = personal.careerName,
        careerCode = personal.careerCode,
        scholarship = personal.scholarship,
        grade = record.stats.grade,
        enrolledSubjects = record.stats.enrolledSubjects,
        enrolledCredits = record.stats.enrolledCredits,
        approvedSubjects = record.stats.approvedSubjects,
        approvedCredits = record.stats.approvedCredits,
        retiredSubjects = record.stats.retiredSubjects,
        retiredCredits = record.stats.retiredCredits,
        failedSubjects = record.stats.failedSubjects,
        failedCredits = record.stats.failedCredits,
        lastUpdate = Date(),
        approvedRelation = record.stats.approvedCredits.toDouble() / record.stats.enrolledCredits,
        appVersionCode = BuildConfig.VERSION_CODE
)

fun DstQuarter.toQuarter(uid: String): Quarter {
    val qid = generateQuarterId(uid = uid, startDate = startDate, endDate = endDate)

    return Quarter(
            id = qid,
            startDate = startDate,
            endDate = endDate,
            grade = grade,
            gradeSum = gradeSum,
            credits = subjects.computeCredits(),
            status = status,
            subjects = subjects.map { dstSubject ->
                val sid = generateSubjectId(uid = uid, qid = qid, code = dstSubject.code)

                dstSubject.toSubject(qid = qid, sid = sid)
            }.toMutableList()
    )
}

fun DstEnrollment.toQuarter(uid: String): Quarter {
    val qid = generateQuarterId(uid = uid, startDate = startDate, endDate = endDate)

    return Quarter(
            id = qid,
            startDate = startDate,
            endDate = endDate,
            grade = MAX_QUARTER_GRADE,
            gradeSum = 0.0,
            credits = schedule.computeCredits(),
            status = STATUS_QUARTER_CURRENT,
            subjects = schedule.map { scheduleSubject ->
                val sid = generateSubjectId(uid = uid, qid = qid, code = scheduleSubject.code)

                scheduleSubject.toSubject(qid = qid, sid = sid)
            }.toMutableList()
    )
}

fun DstSubject.toSubject(qid: String, sid: String) = Subject(
        id = sid,
        qid = qid,
        code = code,
        name = name,
        credits = credits,
        grade = grade,
        status = status.formatSubjectStatusValue()
)

fun ScheduleSubject.toSubject(sid: String, qid: String) = Subject(
        id = sid,
        qid = qid,
        code = code,
        name = name,
        credits = credits,
        grade = MAX_SUBJECT_GRADE,
        status = status.formatSubjectStatusValue()
)