package com.gdavidpb.tuindice.abstracts

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View

abstract class UpdatableFragment : Fragment(), Initializer {
    abstract fun onUpdate(instanceState: Bundle?)

    init {
        arguments = Bundle()
    }

    @JvmOverloads fun update(instanceState: Bundle? = null) {
        if (instanceState != null) {
            arguments?.clear()
            arguments?.putAll(instanceState)
        }

        if (view != null)
            onUpdate(instanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onInitialize(view)
        onUpdate(arguments)
    }
}