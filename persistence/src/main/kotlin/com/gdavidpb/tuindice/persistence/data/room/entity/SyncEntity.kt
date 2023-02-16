package com.gdavidpb.tuindice.persistence.data.room.entity

import androidx.room.*
import com.gdavidpb.tuindice.persistence.data.room.model.SyncEntityAction
import com.gdavidpb.tuindice.persistence.data.room.model.SyncEntityType
import com.gdavidpb.tuindice.persistence.data.room.schema.EvaluationTable
import com.gdavidpb.tuindice.persistence.data.room.schema.QuarterTable
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectTable
import com.gdavidpb.tuindice.persistence.data.room.schema.SyncTable

@Entity(
	tableName = SyncTable.TABLE_NAME,
	foreignKeys = [
		ForeignKey(
			entity = QuarterEntity::class,
			parentColumns = [QuarterTable.ID],
			childColumns = [SyncTable.ENTITY_ID],
			onDelete = ForeignKey.CASCADE,
			onUpdate = ForeignKey.CASCADE
		),
		ForeignKey(
			entity = SubjectEntity::class,
			parentColumns = [SubjectTable.ID],
			childColumns = [SyncTable.ENTITY_ID],
			onDelete = ForeignKey.CASCADE,
			onUpdate = ForeignKey.CASCADE
		),
		ForeignKey(
			entity = EvaluationEntity::class,
			parentColumns = [EvaluationTable.ID],
			childColumns = [SyncTable.ENTITY_ID],
			onDelete = ForeignKey.CASCADE,
			onUpdate = ForeignKey.CASCADE
		)
	],
	indices = [
		Index(value = [SyncTable.ENTITY_ID, SyncTable.ENTITY_ACTION], unique = true)
	]
)
data class SyncEntity(
	@PrimaryKey @ColumnInfo(name = SyncTable.ID) val id: String,
	@ColumnInfo(name = SyncTable.ENTITY_ID) val entityId: String,
	@ColumnInfo(name = SyncTable.ENTITY_TYPE) val entityType: SyncEntityType,
	@ColumnInfo(name = SyncTable.ENTITY_ACTION) val entityAction: SyncEntityAction,
	@ColumnInfo(name = SyncTable.ENTITY_DATA) val entityData: String,
	@ColumnInfo(name = SyncTable.TIMESTAMP) val timestamp: Long
)