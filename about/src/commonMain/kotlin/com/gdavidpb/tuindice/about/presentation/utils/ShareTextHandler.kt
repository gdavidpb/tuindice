package com.gdavidpb.tuindice.about.presentation.utils

fun interface ShareTextHandler {
	operator fun invoke(subject: String, text: String)
}