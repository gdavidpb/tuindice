package com.gdavidpb.tuindice.base.utils.extensions

inline fun Any?.isNull(exec: () -> Unit) = this ?: exec()