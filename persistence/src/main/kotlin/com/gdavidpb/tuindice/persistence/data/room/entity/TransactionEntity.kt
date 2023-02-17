package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.model.TransactionAction
import com.gdavidpb.tuindice.persistence.data.room.model.TransactionStatus
import com.gdavidpb.tuindice.persistence.data.room.model.TransactionType
import com.gdavidpb.tuindice.persistence.data.room.schema.TransactionTable

@Entity(
	tableName = TransactionTable.TABLE_NAME
)
data class TransactionEntity(
	@ColumnInfo(name = TransactionTable.REFERENCE) val reference: String,
	@ColumnInfo(name = TransactionTable.TYPE) val type: TransactionType,
	@ColumnInfo(name = TransactionTable.ACTION) val action: TransactionAction,
	@ColumnInfo(name = TransactionTable.STATUS) val status: TransactionStatus,
	@ColumnInfo(name = TransactionTable.TIMESTAMP) val timestamp: Long = System.currentTimeMillis(),
	@PrimaryKey @ColumnInfo(name = TransactionTable.ID) val id: String = "$action:$reference"
)