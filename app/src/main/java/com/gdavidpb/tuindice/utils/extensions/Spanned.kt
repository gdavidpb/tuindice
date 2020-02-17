package com.gdavidpb.tuindice.utils.extensions

import android.text.SpannableStringBuilder
import android.text.Spanned

inline fun buildSpanned(f: SpannableStringBuilder.() -> Unit): Spanned =
        SpannableStringBuilder().apply(f)

fun SpannableStringBuilder.append(text: CharSequence, vararg spans: Any) {
    val textLength = text.length
    append(text)
    spans.forEach { span ->
        setSpan(span, this.length - textLength, length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
}