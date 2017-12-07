package com.gdavidpb.tuindice.models

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import android.support.v4.util.LruCache
import com.gdavidpb.tuindice.*
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class SQLiteHelper(val context: Context, name: String = "database.sqlite", version: Int = 2)
    : SQLiteOpenHelper(context, name, null, version) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: SQLiteHelper? = null

        @Synchronized
        fun getInstance(context: Context): SQLiteHelper {
            if (instance == null)
                instance = SQLiteHelper(context)

            return instance!!
        }
    }

    private val database by lazy { writableDatabase }

    private val cache = LruCache<Long, Double>(4 * 1024 * 1024)

    /* Database constants */
    private val _accounts = "accounts"
    private val _quarters = "quarters"
    private val _subjects = "subjects"

    private val _createAccounts =
            "CREATE TABLE $_accounts(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "active INTEGER DEFAULT 0," +
                    "temporary INTEGER DEFAULT 0 UNIQUE," +
                    "usbId TEXT NOT NULL UNIQUE," +
                    "password TEXT NOT NULL," +
                    "firstNames TEXT NOT NULL," +
                    "lastNames TEXT NOT NULL," +
                    "careerName TEXT NOT NULL," +
                    "careerCode INTEGER," +
                    "lastUpdate INTEGER," +
                    "failedCredits INTEGER," +
                    "failedSubjects INTEGER," +
                    "retiredCredits INTEGER," +
                    "retiredSubjects INTEGER," +
                    "approvedCredits INTEGER," +
                    "approvedSubject INTEGER," +
                    "enrolledCredits INTEGER," +
                    "enrolledSubjects INTEGER)"

    private val _createQuarters =
            "CREATE TABLE $_quarters(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "aid INTEGER," +
                    "type INTEGER," +
                    "startTime INTEGER UNIQUE," +
                    "endTime INTEGER UNIQUE," +
                    "grade REAL," +
                    "gradeSum REAL)"

    private val _createSubjects =
            "CREATE TABLE $_subjects(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "qid INTEGER," +
                    "aid INTEGER," +
                    "code TEXT NOT NULL," +
                    "name TEXT NOT NULL," +
                    "credits INTEGER," +
                    "grade INTEGER DEFAULT 0," +
                    "status INTEGER DEFAULT 0)"

    override fun onCreate(database: SQLiteDatabase) {
        database.execSQL(_createAccounts)
        database.execSQL(_createQuarters)
        database.execSQL(_createSubjects)
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) { }

    override fun close() {
        super.close()

        instance = null
    }

    fun getActiveAccount(): DstAccount {
        val account: DstAccount
        val cursor = database.rawQuery("SELECT * FROM $_accounts WHERE active = 1")

        if (cursor.moveToFirst()) {
            account = DstAccount(
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

            cursor.close()

            account.quarters.addAll(getQuarters(account.id, QuarterType.ALL))
        } else
            account = DstAccount()

        return account
    }

    fun removeActiveAccount() {
        val cursor = database.rawQuery("SELECT id FROM $_accounts WHERE active = 1")

        if (cursor.moveToFirst()) {
            val id = cursor.getLong(0)

            database.execSQL("DELETE FROM $_subjects WHERE aid = $id")
            database.execSQL("DELETE FROM $_quarters WHERE aid = $id")
            database.execSQL("DELETE FROM $_accounts WHERE id = $id")

            cursor.close()
        }
    }

    fun removeTemporaryAccount() {
        val cursor = database.rawQuery("SELECT id FROM $_accounts WHERE temporary = 1")

        if (cursor.moveToFirst()) {
            val id = cursor.getLong(0)

            database.execSQL("DELETE FROM $_subjects WHERE aid = $id")
            database.execSQL("DELETE FROM $_quarters WHERE aid = $id")
            database.execSQL("DELETE FROM $_accounts WHERE id = $id")

            cursor.close()
        }
    }

    fun setTemporaryAccount(account: DstAccount) {
        addAccount(account, false, true)
    }

    fun getTemporaryAccount(): DstAccount {
        val account: DstAccount
        val cursor = database.rawQuery("SELECT * FROM $_accounts WHERE temporary = 1")

        if (cursor.moveToFirst()) {
            account = DstAccount(
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

            cursor.close()

            account.quarters.addAll(getQuarters(account.id, QuarterType.ALL))
        } else
            account = DstAccount()

        return account
    }

    fun addAccount(account: DstAccount, active: Boolean = false, temporary: Boolean = false) {
        val values = ContentValues()

        if (active) {
            values.put("active", 0)

            database.update(_accounts, values, "active = 1", null)
        }

        values.put("active", if (active) 1 else 0)
        values.put("temporary", if (temporary) 1 else 0)
        values.put("usbId", account.usbId)
        values.put("password", account.password)
        values.put("firstNames", account.firstNames)
        values.put("lastNames", account.lastNames)
        values.put("careerName", account.careerName)
        values.put("careerCode", account.careerCode)
        values.put("lastUpdate", account.lastUpdate)
        values.put("failedCredits", account.failedCredits)
        values.put("failedSubjects", account.failedSubjects)
        values.put("retiredCredits", account.retiredCredits)
        values.put("retiredSubjects", account.retiredSubjects)
        values.put("approvedCredits", account.approvedCredits)
        values.put("approvedSubject", account.approvedSubject)
        values.put("enrolledCredits", account.enrolledCredits)
        values.put("enrolledSubjects", account.enrolledSubjects)

        val accountId = database.insertWithOnConflict(_accounts, null, values, CONFLICT_REPLACE)

        database.execSQL("DELETE FROM $_subjects WHERE aid = $accountId")
        database.execSQL("DELETE FROM $_quarters WHERE aid = $accountId")

        for (quarter in account.quarters)
            addQuarter(accountId, quarter)
    }

    fun getQuarterCredits(quarter: DstQuarter): Int {
        var credits = 0
        val cursor = database.rawQuery("SELECT SUM(credits) FROM $_subjects WHERE status = ${SubjectStatus.OK.value} AND qid = ${quarter.id}")

        if (cursor.moveToFirst()) {
            credits = cursor.getInt(0)

            cursor.close()
        }

        return credits
    }

    fun getQuarterSample(type: QuarterType, status: SubjectStatus = SubjectStatus.OK): DstQuarter {
        val quarter: DstQuarter
        val cursor = database.rawQuery("SELECT * FROM $_quarters WHERE type = ${type.value}")

        val grade = 3.0
        val gradeSum = randomGrade(grade, 5.0)

        if (cursor.moveRandom()) {
            quarter = DstQuarter(
                    QuarterType.fromValue(cursor.getInt(2)),
                    cursor.getLong(3),
                    cursor.getLong(4),
                    cursor.getDouble(5),
                    cursor.getDouble(6))

            quarter.id = cursor.getLong(0)
            quarter.aid = cursor.getLong(1)
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

        cursor.close()

        return quarter
    }

    fun computeGradeFromQuarter(quarter: DstQuarter): Double {
        var grade = 0.0
        val cursor = database.rawQuery("SELECT (1.0 * SUM(credits * grade) / SUM(credits)) FROM $_subjects WHERE status = ${SubjectStatus.OK.value} AND qid = ${quarter.id}")

        if (cursor.moveToFirst()) {
            grade = cursor.getDouble(0).toGrade()

            cursor.close()
        }

        return grade
    }

    fun computeGradeSumFromQuarter(quarter: DstQuarter): Double {
        val key = quarter.getCacheKey()
        var grade = cache.get(key)

        if (grade == null) {
            val cursor = database.rawQuery("SELECT (1.0 * SUM(credits * grade) / SUM(credits)) FROM $_subjects WHERE status = ${SubjectStatus.OK.value} AND aid = ${quarter.aid} AND qid <= ${quarter.id} AND id NOT IN (SELECT id FROM $_subjects WHERE qid < ${quarter.id} AND grade < 3 AND status = ${SubjectStatus.OK.value} AND code IN (SELECT code FROM $_subjects WHERE status = ${SubjectStatus.OK.value} AND qid = ${quarter.id} AND grade >= 3) GROUP BY code)")

            if (cursor.moveToFirst()) {
                grade = cursor.getDouble(0).toGrade()

                cursor.close()
            }

            cache.put(key, grade)
        }

        return grade
    }

    fun updateSubject(subject: DstSubject) {
        val values = ContentValues()

        values.put("code", subject.code)
        values.put("name", subject.name)
        values.put("credits", subject.credits)
        values.put("grade", subject.grade)
        values.put("status", subject.status.value)

        database.update(_subjects, values, "id = ${subject.id}", null)
    }

    fun generateReport(): File? {
        return try {
            val reportDatabase = "report-database"
            val reportOperation = "report-operation"
            val reportName = "report-${Date().time}.zip"

            val databaseFile = File(database.path)
            val databaseCloneFile = File(databaseFile.parent, reportDatabase)
            val operationFile = File(context.filesDir, reportOperation)

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
        val values = ContentValues()

        values.put("usbId", "")
        values.put("password", "")
        values.put("firstNames", "")
        values.put("lastNames", "")

        database.update(_accounts, values, null, null)
    }

    private fun getSubjectSample(status: SubjectStatus = SubjectStatus.OK): DstSubject {
        val subject: DstSubject
        val cursor = database.rawQuery("SELECT * FROM $_subjects WHERE status = ${status.value}")

        if (cursor.moveRandom()) {
            subject = DstSubject(
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getInt(5),
                    cursor.getInt(6),
                    SubjectStatus.fromValue(cursor.getInt(7)))

            subject.grade = 3

            subject.id = cursor.getLong(0)
        } else
            subject = DstSubject("MA1111",
                    "Matematicas I",
                    4,
                    3,
                    status)

        cursor.close()

        return subject
    }

    private fun getSubjects(quarterId: Long): ArrayList<DstSubject> {
        val subjects = ArrayList<DstSubject>()

        val cursor = database.rawQuery("SELECT * FROM $_subjects WHERE qid = $quarterId")

        if (cursor.moveToFirst())
            do {
                val subject = DstSubject(
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getInt(5),
                        cursor.getInt(6),
                        SubjectStatus.fromValue(cursor.getInt(7)))

                subject.id = cursor.getLong(0)

                subjects.add(subject)
            } while (cursor.moveToNext())

        cursor.close()

        return subjects
    }

    private fun getQuarters(accountId: Long, type: QuarterType): ArrayList<DstQuarter> {
        val quarters = ArrayList<DstQuarter>()

        val cursor = if (type == QuarterType.ALL)
            database.rawQuery("SELECT * FROM $_quarters WHERE aid = $accountId")
        else
            database.rawQuery("SELECT * FROM $_quarters WHERE aid = $accountId AND type = ${type.value}")

        if (cursor.moveToFirst())
            do {
                val quarter = DstQuarter(
                        QuarterType.fromValue(cursor.getInt(2)),
                        cursor.getLong(3),
                        cursor.getLong(4),
                        cursor.getDouble(5),
                        cursor.getDouble(6))

                quarter.id = cursor.getLong(0)
                quarter.aid = cursor.getLong(1)

                val subjects = getSubjects(quarter.id)

                quarter.subjects.addAll(subjects)

                quarters.add(quarter)
            } while (cursor.moveToNext())

        cursor.close()

        return quarters
    }

    private fun addQuarter(accountId: Long, quarter: DstQuarter) {
        val values = ContentValues()

        values.put("aid", accountId)
        values.put("startTime", quarter.startTime)
        values.put("endTime", quarter.endTime)
        values.put("grade", quarter.grade)
        values.put("gradeSum", quarter.gradeSum)
        values.put("type", quarter.type.value)

        val quarterId = database.insertWithOnConflict(_quarters, null, values, CONFLICT_REPLACE)

        values.clear()

        database.execSQL("DELETE FROM $_subjects WHERE qid = $quarterId")

        for (subject in quarter.subjects) {
            values.put("qid", quarterId)
            values.put("aid", accountId)
            values.put("code", subject.code)
            values.put("name", subject.name)
            values.put("credits", subject.credits)
            values.put("grade", subject.grade)
            values.put("status", subject.status.value)

            database.insert(_subjects, null, values)
        }
    }

    private fun randomGrade(min: Double, max: Double): Double {
        val maxi = max.toInt() * 10000
        val mini = min.toInt() * 10000

        return (Math.abs(Random().nextInt(maxi + 1 - mini)) + mini) / 10000.0
    }
}