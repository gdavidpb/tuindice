package com.gdavidpb.tuindice.utils.extensions

import androidx.annotation.StringRes
import com.gdavidpb.tuindice.data.utils.Validation
import com.google.android.material.textfield.TextInputLayout
import org.jetbrains.anko.sdk27.coroutines.textChangedListener

fun Array<Validation>.setUp() {
    forEach { set ->
        with(set.view) {
            editText.notNull { editText ->
                editText.textChangedListener {
                    afterTextChanged { error = null }
                }
            }
        }
    }
}

fun Array<Validation>.firstInvalid(onFound: (TextInputLayout.() -> Unit)? = null) = firstOrNull { set ->
    set.run {
        validator(view).also { invalid ->
            if (invalid) {
                with(view) {
                    error = context.getString(set.resource)
                    onFound.notNull { it.invoke(this) }
                }
            }
        }
    }
}

infix fun TextInputLayout.set(@StringRes resource: Int) = this to resource

infix fun Pair<TextInputLayout, Int>.`when`(valid: TextInputLayout.() -> Boolean) = Validation(first, second, valid)
