package com.gdavidpb.tuindice.base.utils.extension

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity

inline val FragmentActivity.contentView: View?
    get() = findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0)