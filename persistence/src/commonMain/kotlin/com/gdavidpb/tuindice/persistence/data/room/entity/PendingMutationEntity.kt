package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gdavidpb.tuindice.persistence.data.room.schema.PendingMutationTable

@Entity(
	tableName = PendingMutationTable.TABLE_NAME,
	indices = [
		Index(value = [PendingMutationTable.REPLACE_KEY], unique = true),
		Index(value = [PendingMutationTable.ENTITY_TYPE, PendingMutationTable.ENTITY_ID])
	]
)
data class PendingMutationEntity(
	@PrimaryKey
	@ColumnInfo(name = PendingMutationTable.MUTATION_ID)
	val mutationId: String,
	@ColumnInfo(name = PendingMutationTable.ENTITY_TYPE)
	val entityType: String,
	@ColumnInfo(name = PendingMutationTable.ENTITY_ID)
	val entityId: String,
	@ColumnInfo(name = PendingMutationTable.REPLACE_KEY)
	val replaceKey: String,
	@ColumnInfo(name = PendingMutationTable.PAYLOAD)
	val payload: String,
	@ColumnInfo(name = PendingMutationTable.EXPECTED_REVISION)
	val expectedRevision: Long,
	@ColumnInfo(name = PendingMutationTable.STATUS)
	val status: String,
	@ColumnInfo(name = PendingMutationTable.CREATED_AT)
	val createdAt: Long,
	@ColumnInfo(name = PendingMutationTable.UPDATED_AT)
	val updatedAt: Long,
	@ColumnInfo(name = PendingMutationTable.LAST_ERROR)
	val lastError: String?
)

