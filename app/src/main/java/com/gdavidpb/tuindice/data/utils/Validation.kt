package com.gdavidpb.tuindice.data.utils

import androidx.annotation.StringRes
import com.google.android.material.textfield.TextInputLayout

class Validation(val view: TextInputLayout,
                 @StringRes val resource: Int,
                 val validator: (TextInputLayout.() -> Boolean))
