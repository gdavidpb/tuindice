package com.gdavidpb.tuindice.base.utils.extension

fun <T> tryGetOrNull(block: () -> T) = runCatching(block).getOrNull()
