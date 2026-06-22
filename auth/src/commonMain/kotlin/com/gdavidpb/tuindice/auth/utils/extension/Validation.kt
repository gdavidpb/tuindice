package com.gdavidpb.tuindice.auth.utils.extension

private val usbIdRegex = "^\\d{2}-\\d{5}$".toRegex()
private val compactUsbIdRegex = "^\\d{7}$".toRegex()
private val invalidNumericLikeUsbEmailRegex = "^[0-9-]+$".toRegex()
private val usbEmailRegex =
	"""^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*(@usb\.ve)?$"""
		.toRegex(RegexOption.IGNORE_CASE)
private const val USB_EMAIL_SUFFIX = "@usb.ve"

fun String.isUsbId(): Boolean {
	val value = trim()
	return value.matches(usbIdRegex) || value.matches(compactUsbIdRegex)
}

fun String.isUsbEmail(): Boolean {
	val value = trim()
	val localPart = value.lowercase().removeSuffix(USB_EMAIL_SUFFIX)
	val isValidUsbId = localPart.isUsbId()
	val isValidUsbEmail =
		!localPart.matches(invalidNumericLikeUsbEmailRegex) &&
			value.matches(usbEmailRegex)

	return value.isNotEmpty() &&
		value.count { character -> character == '@' } <= 1 &&
		(isValidUsbId || isValidUsbEmail)
}

fun String.toCanonicalUsbIdentifier(): String {
	val value = trim().lowercase()
	val localPart = value.removeSuffix(USB_EMAIL_SUFFIX)

	return when {
		localPart.matches(compactUsbIdRegex) -> "${localPart.take(2)}-${localPart.drop(2)}"
		else -> localPart
	}
}
