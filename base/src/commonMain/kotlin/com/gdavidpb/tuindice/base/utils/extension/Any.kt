package com.gdavidpb.tuindice.base.utils.extension

fun Any.eventName(): String {
	val simpleName = this::class.simpleName ?: this::class.toString().substringAfterLast('.')
	return simpleName.toSnakeCase()
}
