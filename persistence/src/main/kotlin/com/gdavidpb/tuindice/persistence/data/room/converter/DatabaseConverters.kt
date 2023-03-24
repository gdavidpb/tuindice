package com.gdavidpb.tuindice.persistence.data.room.converter

import androidx.room.TypeConverter
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionType
import java.util.*

class DatabaseConverters {
	@TypeConverter
	fun dateToLong(value: Date): Long = value.time

	@TypeConverter
	fun longToDate(value: Long): Date = Date(value)

	@TypeConverter
	fun intToBoolean(value: Int): Boolean = value != 0

	@TypeConverter
	fun booleanToInt(value: Boolean): Int = value.compareTo(false)

	@TypeConverter
	fun evaluationTypeToInt(value: EvaluationType): Int = value.ordinal

	@TypeConverter
	fun intToEvaluationType(value: Int): EvaluationType = EvaluationType.values()[value]

	@TypeConverter
	fun transactionActionToInt(value: TransactionAction): Int = value.ordinal

	@TypeConverter
	fun intToTransactionAction(value: Int): TransactionAction = TransactionAction.values()[value]

	@TypeConverter
	fun transactionTypeToInt(value: TransactionType): Int = value.ordinal

	@TypeConverter
	fun intToTransactionType(value: Int): TransactionType = TransactionType.values()[value]
}