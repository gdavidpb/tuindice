package com.gdavidpb.tuindice.base.utils.extension

import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

@Deprecated("This will be removed", ReplaceWith("lazy { findViewById<T>(id) }"))
fun <T : View> View.view(@IdRes id: Int) = lazy { findViewById<T>(id) }

@Deprecated("This will be removed", ReplaceWith("lazy { findViewById<T>(id) }"))
fun <T : View> Fragment.view(@IdRes id: Int) = lazy { requireView().findViewById<T>(id) }

@Deprecated("This will be removed", ReplaceWith("lazy { findViewById<T>(id) }"))
fun <T : View> FragmentActivity.view(@IdRes id: Int) = lazy { findViewById<T>(id) }