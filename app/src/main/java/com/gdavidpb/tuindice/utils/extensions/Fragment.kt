package com.gdavidpb.tuindice.utils.extensions

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

fun Fragment.selector(
        @StringRes textResource: Int,
        items: Array<String>,
        onClick: (String) -> Unit
) = requireActivity().selector(textResource, items, onClick)

fun Fragment.email(email: String, subject: String = "", text: String = "") = requireContext().email(email, subject, text)

fun Fragment.share(text: String, subject: String = "") = requireContext().share(text, subject)