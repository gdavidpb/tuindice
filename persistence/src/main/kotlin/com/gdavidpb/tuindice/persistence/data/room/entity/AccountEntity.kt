package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.AccountTable
import java.util.*

@Entity(
	tableName = AccountTable.TABLE_NAME,
	indices = [
		Index(value = [AccountTable.ID, AccountTable.CARD_ID], unique = true),
		Index(value = [AccountTable.ID, AccountTable.USB_ID], unique = true),
		Index(value = [AccountTable.ID, AccountTable.EMAIL], unique = true)
	]
)
data class AccountEntity(
	@PrimaryKey @ColumnInfo(name = AccountTable.ID) val id: String,
	@ColumnInfo(name = AccountTable.CARD_ID) val cid: String,
	@ColumnInfo(name = AccountTable.USB_ID) val usbId: String,
	@ColumnInfo(name = AccountTable.EMAIL) val email: String,
	@ColumnInfo(name = AccountTable.PICTURE_URL) val pictureUrl: String,
	@ColumnInfo(name = AccountTable.FULL_NAME) val fullName: String,
	@ColumnInfo(name = AccountTable.FIRST_NAMES) val firstNames: String,
	@ColumnInfo(name = AccountTable.LAST_NAMES) val lastNames: String,
	@ColumnInfo(name = AccountTable.CAREER_NAME) val careerName: String,
	@ColumnInfo(name = AccountTable.CAREER_CODE) val careerCode: Int,
	@ColumnInfo(name = AccountTable.SCHOLARSHIP) val scholarship: Boolean,
	@ColumnInfo(name = AccountTable.GRADE) val grade: Double,
	@ColumnInfo(name = AccountTable.ENROLLED_SUBJECTS) val enrolledSubjects: Int,
	@ColumnInfo(name = AccountTable.ENROLLED_CREDITS) val enrolledCredits: Int,
	@ColumnInfo(name = AccountTable.APPROVED_SUBJECTS) val approvedSubjects: Int,
	@ColumnInfo(name = AccountTable.APPROVED_CREDITS) val approvedCredits: Int,
	@ColumnInfo(name = AccountTable.RETIRED_SUBJECTS) val retiredSubjects: Int,
	@ColumnInfo(name = AccountTable.RETIRED_CREDITS) val retiredCredits: Int,
	@ColumnInfo(name = AccountTable.FAILED_SUBJECTS) val failedSubjects: Int,
	@ColumnInfo(name = AccountTable.FAILED_CREDITS) val failedCredits: Int,
	@ColumnInfo(name = AccountTable.LAST_UPDATE) val lastUpdate: Long
)
