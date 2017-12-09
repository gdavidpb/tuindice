package com.gdavidpb.tuindice.models

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.support.v4.util.LruCache
import com.gdavidpb.tuindice.*
import org.jetbrains.anko.db.*
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

val Context.database: SQLiteHelper
    get() = SQLiteHelper.getInstance(applicationContext)

class SQLiteHelper(context: Context, name: String = "database.sqlite", version: Int = 2)
    : ManagedSQLiteOpenHelper(context, name, null, version) {

    companion object {
        private var instance: SQLiteHelper? = null

        @Synchronized
        fun getInstance(context: Context): SQLiteHelper {
            if (instance == null)
                instance = SQLiteHelper(context.applicationContext)

            return instance!!
        }
    }

    private val database by lazy { context.database }
    private val cache by lazy { LruCache<Long, Double>(4 * 1024 * 1024) }

    /* Database constants */
    private val _accounts = "accounts"
    private val _quarters = "quarters"
    private val _subjects = "subjects"

    override fun onCreate(database: SQLiteDatabase) {
        /* Create tables */
        database.createTable(_accounts, true,
                "id" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                "active" to INTEGER + DEFAULT("0"),
                "temporary" to INTEGER + UNIQUE + DEFAULT("0"),
                "usbId" to TEXT + NOT_NULL + UNIQUE,
                "password" to TEXT + NOT_NULL,
                "firstNames" to TEXT + NOT_NULL,
                "lastNames" to TEXT + NOT_NULL,
                "careerName" to TEXT + NOT_NULL,
                "careerCode" to INTEGER,
                "lastUpdate" to INTEGER,
                "failedCredits" to INTEGER,
                "failedSubjects" to INTEGER,
                "retiredCredits" to INTEGER,
                "retiredSubjects" to INTEGER,
                "approvedCredits" to INTEGER,
                "approvedSubject" to INTEGER,
                "enrolledCredits" to INTEGER,
                "enrolledSubjects" to INTEGER)

        database.createTable(_quarters, true,
                "id" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                        "aid" to INTEGER,
                        "type" to INTEGER,
                        "startTime" to INTEGER + UNIQUE,
                        "endTime" to INTEGER + UNIQUE,
                        "grade" to REAL,
                        "gradeSum" to REAL)

        database.createTable(_subjects, true,
                "id" to INTEGER + PRIMARY_KEY + AUTOINCREMENT,
                        "qid" to INTEGER,
                        "aid" to INTEGER,
                        "code" to TEXT + NOT_NULL,
                        "name" to TEXT + NOT_NULL,
                        "credits" to INTEGER,
                        "grade" to INTEGER + DEFAULT("0"),
                        "status" to INTEGER + DEFAULT("0"))
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) { }

    override fun close() {
        super.close()

        instance = null
    }

    fun getActiveAccount(): DstAccount {
        return database.use {
            select(_accounts).whereArgs("active = 1").exec {
                if (moveToFirst())
                    parseAccount(this)
                else
                    DstAccount()
            }
        }
    }

    fun removeActiveAccount() {
        database.use {
            select(_accounts, "id")
                    .whereArgs("active = 1").exec {
                if (moveToFirst()) {
                    val accountId = getLong(0)

                    delete(_subjects, "aid = $accountId")
                    delete(_quarters, "aid = $accountId")
                    delete(_accounts, "id = $accountId")
                }
            }
        }
    }

    fun removeTemporaryAccount() {
        database.use { delete(_accounts, "temporary = 1") }
    }

    fun setTemporaryAccount(account: DstAccount) {
        database.use {
            val n = update(_accounts,
                    "usbId" to account.usbId,
                    "password" to account.password)
                    .whereArgs("temporary = 1")
                    .exec()

            if (n == 0)
                insert(_accounts,
                        "usbId" to account.usbId,
                        "password" to account.password,
                        "firstNames" to "",
                        "lastNames" to "",
                        "careerName" to "",
                        "temporary" to 1)
        }
    }

    fun getTemporaryAccount(): DstAccount {
        return database.use {
            select(_accounts).whereArgs("temporary = 1").exec {
                if (moveToFirst()) {
                    val account = DstAccount(getString(3), getString(4))

                    account.id = getLong(0)

                    account
                } else
                    DstAccount()
            }
        }
    }

    fun addAccount(account: DstAccount, active: Boolean = false) {
        database.use {
            update(_accounts, "active" to 0)
                    .whereArgs("active = 1")
                    .exec()

            val accountId = replace(_accounts,
                    "active" to if (active) 1 else 0,
                    "temporary" to 0,
                    "usbId" to account.usbId,
                    "password" to account.password,
                    "firstNames" to account.firstNames,
                    "lastNames" to account.lastNames,
                    "careerName" to account.careerName,
                    "careerCode" to account.careerCode,
                    "lastUpdate" to account.lastUpdate,
                    "failedCredits" to account.failedCredits,
                    "failedSubjects" to account.failedSubjects,
                    "retiredCredits" to account.retiredCredits,
                    "retiredSubjects" to account.retiredSubjects,
                    "approvedCredits" to account.approvedCredits,
                    "approvedSubject" to account.approvedSubject,
                    "enrolledCredits" to account.enrolledCredits,
                    "enrolledSubjects" to account.enrolledSubjects)

            for (quarter in account.quarters)
                addQuarter(accountId, quarter)

            computeGradesForCurrentQuarter(accountId)
        }
    }

    fun updateAccount(accountId: Long, account: DstAccount) {
        database.use {
            update(_accounts,
                    "firstNames" to account.firstNames,
                    "lastNames" to account.lastNames,
                    "careerName" to account.careerName,
                    "careerCode" to account.careerCode,
                    "lastUpdate" to account.lastUpdate,
                    "failedCredits" to account.failedCredits,
                    "failedSubjects" to account.failedSubjects,
                    "retiredCredits" to account.retiredCredits,
                    "retiredSubjects" to account.retiredSubjects,
                    "approvedCredits" to account.approvedCredits,
                    "approvedSubject" to account.approvedSubject,
                    "enrolledCredits" to account.enrolledCredits,
                    "enrolledSubjects" to account.enrolledSubjects)
                    .whereArgs("id = $accountId").exec()

            /* Remove current quarter */
            val quarterId = select(_quarters, "id")
                    .whereArgs("aid = $accountId AND type = ${QuarterType.CURRENT.value}")
                    .exec { if (moveToFirst()) getLong(0) else -1L }

            if (quarterId != -1L) {
                delete(_subjects, "qid = $quarterId")
                delete(_quarters, "id = $quarterId")
            }

            for (quarter in account.quarters)
                addQuarter(accountId, quarter)

            computeGradesForCurrentQuarter(accountId)
        }
    }

    fun updateSubject(subject: DstSubject) {
        database.use {
            update(_subjects,
                    "code" to subject.code,
                    "name" to subject.name,
                    "credits" to subject.credits,
                    "grade" to subject.grade,
                    "status" to subject.status.value)
                    .whereArgs("id = ${subject.id}")
                    .exec()
        }
    }

    fun getQuarterCredits(quarter: DstQuarter): Int {
        return database.use {
            select(_subjects, "SUM(credits)")
                    .whereArgs("qid = ${quarter.id} AND status = ${SubjectStatus.OK.value}")
                    .exec { if (moveToFirst()) getInt(0) else 0 }
        }
    }

    fun getQuarterSample(type: QuarterType, status: SubjectStatus = SubjectStatus.OK): DstQuarter {
        return database.use {
            select(_quarters)
                    .whereArgs("type = ${type.value}").exec {
                val quarter: DstQuarter

                val grade = 3.0
                val gradeSum = randomGrade(grade, 5.0)

                if (moveRandom()) {
                    quarter = DstQuarter(
                            QuarterType.fromValue(getInt(2)),
                            getLong(3),
                            getLong(4),
                            getDouble(5),
                            getDouble(6))

                    quarter.id = getLong(0)
                    quarter.aid = getLong(1)
                } else {
                    /* Sample quarter */
                    val startTime = Calendar.getInstance()
                    val endTime = Calendar.getInstance()

                    val times = arrayOf(Pair(Calendar.JANUARY, Calendar.MARCH),
                            Pair(Calendar.APRIL, Calendar.JULY),
                            Pair(Calendar.JULY, Calendar.AUGUST),
                            Pair(Calendar.SEPTEMBER, Calendar.DECEMBER))

                    val time = times[Math.abs(Random().nextInt()) % times.size]

                    startTime.set(Calendar.MONTH, time.first)
                    endTime.set(Calendar.MONTH, time.second)

                    quarter = DstQuarter(type,
                            startTime.timeInMillis,
                            endTime.timeInMillis,
                            grade.toGrade(),
                            gradeSum.toGrade())
                }

                val subject = getSubjectSample(status)

                quarter.grade = subject.grade.toDouble()
                quarter.gradeSum = gradeSum

                quarter.subjects.add(subject)

                quarter
            }
        }
    }

    fun computeGradeFromQuarter(quarter: DstQuarter): Double {
        return database.use {
            select(_subjects, "(1.0 * SUM(credits * grade) / SUM(credits))")
                    .whereArgs("qid = ${quarter.id} AND status = ${SubjectStatus.OK.value}")
                    .exec { (if (moveToFirst()) getDouble(0) else 0.0).toGrade() }
        }
    }

    fun computeGradeSumFromQuarter(quarter: DstQuarter): Double {
        val key = quarter.getCacheKey()
        var grade = cache.get(key)

        if (grade == null) {
            grade = database.use {
                select(_subjects, "(1.0 * SUM(credits * grade) / SUM(credits))")
                        .whereArgs("aid = ${quarter.aid} AND qid <= ${quarter.id} AND status = ${SubjectStatus.OK.value} AND id NOT IN (SELECT id FROM $_subjects WHERE qid < ${quarter.id} AND grade < 3 AND status = ${SubjectStatus.OK.value} AND code IN (SELECT code FROM $_subjects WHERE qid = ${quarter.id} AND status = ${SubjectStatus.OK.value} AND grade >= 3) GROUP BY code)")
                        .exec { if (moveToFirst()) getDouble(0) else 0.0 }
            }

            cache.put(key, grade)
        }

        return grade.toGrade()
    }

    fun generateReport(context: Context): File? {
        return try {
            val reportDatabase = "report-database"
            val reportOperation = "report-operation"
            val reportStacktrace = "report-stacktrace"
            val reportName = "report-${Date().time}.zip"

            val databaseFile = context.getDatabasePath(databaseName)
            val databaseCloneFile = File(databaseFile.parent, reportDatabase)
            val operationFile = File(context.filesDir, reportOperation)
            val stacktraceFile = File(context.filesDir, reportStacktrace)

            val account = getActiveAccount()

            /* If there is an active account in database */
            if (!account.isEmpty()) {
                /* Make database clone */
                databaseFile.copyTo(databaseCloneFile, true)

                /* Open database clone */
                val databaseClone = SQLiteHelper(context, reportDatabase)

                /* Remove personal data from database clone */
                databaseClone.makeAnonymous()

                databaseClone.close()
            }

            /* Create report file in external cache directory */
            val reportFile = File(context.externalCacheDir, reportName)

            /* If there is nothing to add to report zip  */
            if (!databaseCloneFile.exists() &&
                    !operationFile.exists() &&
                    !stacktraceFile.exists())
                throw IllegalStateException("There is nothing to include to the report file")

            /* Merge database and operation reports into a report zip */
            val output = ZipOutputStream(FileOutputStream(reportFile))

            /* Put database report in report zip */
            if (databaseCloneFile.exists()) {
                val databaseInput = databaseCloneFile.inputStream()

                output.putEntry(ZipEntry(reportDatabase), databaseInput)

                databaseCloneFile.delete()
            }

            /* Put operation report in report zip */
            if (operationFile.exists()) {
                val operationInput = operationFile.inputStream()

                output.putEntry(ZipEntry(reportOperation), operationInput)

                operationFile.delete()
            }

            /* Put stacktrace report in report zip */
            if (stacktraceFile.exists()) {
                val stackTraceInput = stacktraceFile.inputStream()

                output.putEntry(ZipEntry(reportStacktrace), stackTraceInput)

                stacktraceFile.delete()
            }

            output.close()

            /* Delete report file on exit */
            reportFile.deleteOnExit()

            reportFile
        } catch (exception: Exception) {
            exception.printStackTrace()

            null
        }
    }

    private fun makeAnonymous() {
        database.use {
            update(_accounts,
                    "usbId" to "",
                    "password" to "",
                    "firstNames" to "",
                    "lastNames" to "")
                    .exec()
        }
    }

    private fun randomGrade(min: Double, max: Double): Double {
        val maxi = max.toInt() * 10000
        val mini = min.toInt() * 10000

        return (Math.abs(Random().nextInt(maxi + 1 - mini)) + mini) / 10000.0
    }

    private fun parseSubject(cursor: Cursor): DstSubject {
        val subject = DstSubject(
                cursor.getString(3),
                cursor.getString(4),
                cursor.getInt(5),
                cursor.getInt(6),
                SubjectStatus.fromValue(cursor.getInt(7)))

        subject.id = cursor.getLong(0)

        return subject
    }

    private fun SQLiteDatabase.parseAccount(cursor: Cursor): DstAccount {
        val account = DstAccount(
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7),
                cursor.getInt(8),
                cursor.getLong(9),
                cursor.getInt(10),
                cursor.getInt(11),
                cursor.getInt(12),
                cursor.getInt(13),
                cursor.getInt(14),
                cursor.getInt(15),
                cursor.getInt(16),
                cursor.getInt(17))

        account.id = cursor.getLong(0)

        account.quarters.addAll(getQuarters(account.id, QuarterType.ALL))

        return account
    }

    private fun SQLiteDatabase.parseQuarter(cursor: Cursor): DstQuarter {
        val quarter = DstQuarter(QuarterType.fromValue(cursor.getInt(2)),
                cursor.getLong(3),
                cursor.getLong(4),
                cursor.getDouble(5),
                cursor.getDouble(6))

        quarter.id = cursor.getLong(0)
        quarter.aid = cursor.getLong(1)

        val subjects = getSubjects(quarter.id)

        quarter.subjects.addAll(subjects)

        return quarter
    }

    private fun SQLiteDatabase.computeGradesForCurrentQuarter(accountId: Long) {
        val currentQuarter = select(_quarters)
                .whereArgs("aid = $accountId AND type = ${QuarterType.CURRENT.value}").exec { if (moveToFirst()) parseQuarter(this) else null }

        if (currentQuarter != null) {
            val grade = computeGradeFromQuarter(currentQuarter)
            val gradeSum = computeGradeSumFromQuarter(currentQuarter)

            update(_quarters,
                    "grade" to grade,
                    "gradeSum" to gradeSum)
                    .whereArgs("id = ${currentQuarter.id}")
                    .exec()
        }
    }

    private fun SQLiteDatabase.getSubjectSample(status: SubjectStatus = SubjectStatus.OK): DstSubject {
        return select(_subjects)
                .whereArgs("status = ${status.value}").exec {
            if (moveRandom())
                parseSubject(this).copy(grade = 3)
            else
                DstSubject("MA1111",
                        "Matematicas I",
                        4,
                        3,
                        status)
        }
    }

    private fun SQLiteDatabase.getSubjects(quarterId: Long): ArrayList<DstSubject> {
        return select(_subjects)
                .whereArgs("qid = $quarterId").exec {
            val subjects = ArrayList<DstSubject>()

            if (moveToFirst())
                do
                    subjects.add(parseSubject(this))
                while (moveToNext())

            subjects
        }
    }

    private fun SQLiteDatabase.getQuarters(accountId: Long, type: QuarterType): ArrayList<DstQuarter> {
        return select(_quarters)
                .whereArgs(if (type == QuarterType.ALL) "aid = $accountId" else "aid = $accountId AND type = ${type.value}").exec {
            val quarters = ArrayList<DstQuarter>()

            if (moveToFirst())
                do
                    quarters.add(parseQuarter(this))
                while (moveToNext())

            quarters
        }
    }

    private fun SQLiteDatabase.addQuarter(accountId: Long, quarter: DstQuarter) {
        val quarterId = insert(_quarters,
                "aid" to accountId,
                "startTime" to quarter.startTime,
                "endTime" to quarter.endTime,
                "grade" to quarter.grade,
                "gradeSum" to quarter.gradeSum,
                "type" to quarter.type.value)

        if (quarterId != -1L)
            for (subject in quarter.subjects)
                insert(_subjects,
                        "qid" to quarterId,
                        "aid" to accountId,
                        "code" to subject.code,
                        "name" to subject.name,
                        "credits" to subject.credits,
                        "grade" to subject.grade,
                        "status" to subject.status.value)
    }
}