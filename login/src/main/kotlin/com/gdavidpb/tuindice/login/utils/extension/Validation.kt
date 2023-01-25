package com.gdavidpb.tuindice.login.utils.extension

fun String.isUsbId() = matches("^\\d{2}-\\d{5}$".toRegex())