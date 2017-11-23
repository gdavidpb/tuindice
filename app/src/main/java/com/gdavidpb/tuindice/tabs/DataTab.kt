package com.gdavidpb.tuindice.tabs

import android.os.Bundle
import android.support.v7.content.res.AppCompatResources
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.gdavidpb.tuindice.Constants
import com.gdavidpb.tuindice.DstAccount
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.abstracts.UpdatableFragment
import com.gdavidpb.tuindice.getTintVector
import kotlinx.android.synthetic.main.view_tab_data.*

class DataTab : UpdatableFragment() {

    override fun onInitialize(view: View?) {

    }

    override fun onUpdate(instanceState: Bundle?) {
        val account = instanceState?.getSerializable(Constants.EXTRA_ACCOUNT) as? DstAccount ?: DstAccount()

        if (account.isEmpty())
            return

        /* Set up drawables */
        val drawableAccount = context!!.getTintVector(R.drawable.ic_account, R.color.colorSecondaryText)
        val drawableCareer = AppCompatResources.getDrawable(context!!, R.drawable.ic_career)
        val drawableUid = AppCompatResources.getDrawable(context!!, R.drawable.ic_uid)

        tViewFullName.setCompoundDrawablesWithIntrinsicBounds(drawableAccount, null, null, null)
        tViewCareer.setCompoundDrawablesWithIntrinsicBounds(drawableCareer, null, null, null)
        tViewUID.setCompoundDrawablesWithIntrinsicBounds(drawableUid, null, null, null)

        /* Set up data */
        tViewFullName.text = String.format("%s %s", account.firstNames, account.lastNames)

        tViewCareer.text = getString(R.string.app_title, account.careerCode, account.careerName)

        tViewUID.text = account.usbId
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.view_tab_data, container, false)
}