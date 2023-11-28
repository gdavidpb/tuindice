package com.gdavidpb.tuindice.transactions.domain.annotation

import com.gdavidpb.tuindice.transactions.data.api.transaction.TransactionParser
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
annotation class EnqueuedApi(
	val parser: KClass<out TransactionParser>
)