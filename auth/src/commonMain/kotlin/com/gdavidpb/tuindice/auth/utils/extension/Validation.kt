package com.gdavidpb.tuindice.auth.utils.extension

private val usbIdRegex = "^\\d{2}-\\d{5}$".toRegex()

fun String.isUsbId() = matches(usbIdRegex)