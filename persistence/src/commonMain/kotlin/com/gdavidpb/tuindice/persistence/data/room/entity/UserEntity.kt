package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.UserTable

@Entity(
	tableName = UserTable.TABLE_NAME,
	indices = [
		Index(value = [UserTable.ID, UserTable.CARD_ID], unique = true),
		Index(value = [UserTable.ID, UserTable.USB_ID], unique = true),
		Index(value = [UserTable.ID, UserTable.EMAIL], unique = true)
	]
)
data class UserEntity(
	@PrimaryKey @ColumnInfo(name = UserTable.ID) val id: String,
	@ColumnInfo(name = UserTable.CARD_ID) val cid: String,
	@ColumnInfo(name = UserTable.USB_ID) val usbId: String,
	@ColumnInfo(name = UserTable.EMAIL) val email: String,
	@ColumnInfo(name = UserTable.PICTURE_URL) val pictureUrl: String,
	@ColumnInfo(name = UserTable.FULL_NAME) val fullName: String,
	@ColumnInfo(name = UserTable.FIRST_NAMES) val firstNames: String,
	@ColumnInfo(name = UserTable.LAST_NAMES) val lastNames: String,
	@ColumnInfo(name = UserTable.CAREER_NAME) val careerName: String,
	@ColumnInfo(name = UserTable.CAREER_CODE) val careerCode: Int,
	@ColumnInfo(name = UserTable.SCHOLARSHIP) val scholarship: Boolean,
	@ColumnInfo(name = UserTable.GRADE) val grade: Double,
	@ColumnInfo(name = UserTable.ENROLLED_SUBJECTS) val enrolledSubjects: Int,
	@ColumnInfo(name = UserTable.ENROLLED_CREDITS) val enrolledCredits: Int,
	@ColumnInfo(name = UserTable.APPROVED_SUBJECTS) val approvedSubjects: Int,
	@ColumnInfo(name = UserTable.APPROVED_CREDITS) val approvedCredits: Int,
	@ColumnInfo(name = UserTable.RETIRED_SUBJECTS) val retiredSubjects: Int,
	@ColumnInfo(name = UserTable.RETIRED_CREDITS) val retiredCredits: Int,
	@ColumnInfo(name = UserTable.FAILED_SUBJECTS) val failedSubjects: Int,
	@ColumnInfo(name = UserTable.FAILED_CREDITS) val failedCredits: Int,
	@ColumnInfo(name = UserTable.LAST_UPDATE) val lastUpdate: Long
)