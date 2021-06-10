package com.gdavidpb.tuindice.utils.extensions

inline fun Any?.isNull(exec: () -> Unit) = this ?: exec()