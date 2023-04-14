package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.CounterTable

@Entity(
	tableName = CounterTable.TABLE_NAME,
)
data class CounterEntity(
	@PrimaryKey @ColumnInfo(name = CounterTable.ID) val id: String,
	@ColumnInfo(name = CounterTable.VALUE) val value: Long
)