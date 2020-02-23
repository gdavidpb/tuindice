package com.gdavidpb.tuindice.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.EXTRA_PARCELABLE

abstract class RequestDialog<I : Parcelable, O> : DialogFragment() {

    private var liveData: MutableLiveData<O>? = null

    private val argumentsValue by lazy {
        arguments?.getParcelable<I>(EXTRA_PARCELABLE)
    }

    abstract fun onDialogCreated(arguments: I?)

    override fun onCreateDialog(savedInstanceState: Bundle?) = Dialog(requireActivity(), R.style.AppTheme_FullScreenDialog)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = onDialogCreated(argumentsValue)

    fun show(fragmentManager: FragmentManager, request: I? = null, liveData: MutableLiveData<O>? = null) {
        this.liveData = liveData
        this.arguments = bundleOf(EXTRA_PARCELABLE to request)

        show(fragmentManager, RequestDialog::class.java.name)
    }

    fun callback(response: O) = liveData?.postValue(response)

    fun getRequest() = argumentsValue
}