package com.gdavidpb.tuindice.base.utils.extensions

fun String.isUsbId() = matches("^\\d{2}-\\d{5}$".toRegex())