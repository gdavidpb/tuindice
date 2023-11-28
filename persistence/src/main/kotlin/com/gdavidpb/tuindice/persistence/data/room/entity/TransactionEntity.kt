package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.TransactionTable

@Entity(
	tableName = TransactionTable.TABLE_NAME
)
data class TransactionEntity(
	@PrimaryKey @ColumnInfo(name = TransactionTable.ID) val id: String,
	@ColumnInfo(name = TransactionTable.TIMESTAMP) val timestamp: Long,
	@ColumnInfo(name = TransactionTable.ACCOUNT_ID) val accountId: String,
	@ColumnInfo(name = TransactionTable.REFERENCE) val reference: String,
	@ColumnInfo(name = TransactionTable.TYPE) val type: Int,
	@ColumnInfo(name = TransactionTable.ACTION) val action: Int,
	@ColumnInfo(name = TransactionTable.DATA) val data: String?
)