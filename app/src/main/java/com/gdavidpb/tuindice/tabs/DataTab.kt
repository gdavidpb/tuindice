package com.gdavidpb.tuindice.tabs

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.content.res.AppCompatResources
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.getTintVector
import com.gdavidpb.tuindice.models.database
import kotlinx.android.synthetic.main.view_tab_data.*

class DataTab : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.view_tab_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = context ?: return

        /* Get active account from database */
        val account = context.database.getActiveAccount()

        /* Set up drawables */
        val drawableAccount = context.getTintVector(R.drawable.ic_account, R.color.colorSecondaryText)
        val drawableCareer = AppCompatResources.getDrawable(context, R.drawable.ic_career)
        val drawableUid = AppCompatResources.getDrawable(context, R.drawable.ic_uid)

        tViewFullName.setCompoundDrawablesWithIntrinsicBounds(drawableAccount, null, null, null)
        tViewCareer.setCompoundDrawablesWithIntrinsicBounds(drawableCareer, null, null, null)
        tViewUID.setCompoundDrawablesWithIntrinsicBounds(drawableUid, null, null, null)

        /* Set up data */
        tViewFullName.text = String.format("%s %s", account.firstNames, account.lastNames)

        tViewCareer.text = getString(R.string.app_title, account.careerCode, account.careerName)

        tViewUID.text = account.usbId
    }
}