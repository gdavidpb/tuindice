package com.gdavidpb.tuindice.utils.extensions

import androidx.annotation.StringRes
import com.gdavidpb.tuindice.utils.NO_GETTER
import com.google.android.material.textfield.TextInputLayout

var TextInputLayout.errorResource: Int
    @Deprecated(message = NO_GETTER, level = DeprecationLevel.ERROR) get() = throw NotImplementedError()
    set(@StringRes value) {
        error = context.getString(value)
    }