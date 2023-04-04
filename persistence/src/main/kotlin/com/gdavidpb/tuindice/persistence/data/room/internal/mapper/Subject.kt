package com.gdavidpb.tuindice.persistence.data.room.internal.mapper

import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionAction
import com.gdavidpb.tuindice.base.domain.model.transaction.TransactionType
import com.gdavidpb.tuindice.persistence.data.room.entity.TransactionEntity

fun TransactionEntity.isSubjectUpdate() =
	type == TransactionType.SUBJECT && action == TransactionAction.UPDATE