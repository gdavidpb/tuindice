package com.gdavidpb.tuindice.views

import android.content.Context
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.gdavidpb.tuindice.Constants
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.abstracts.Initializer
import com.gdavidpb.tuindice.hideSoftInput
import com.gdavidpb.tuindice.showSoftInput
import kotlinx.android.synthetic.main.view_dialog_contact.*

class ContactDialog(context: Context) : AlertDialog(context), Initializer {

    init {
        val view = View.inflate(context, R.layout.view_dialog_contact, null)

        setView(view)

        onInitialize(view)
    }

    override fun onInitialize(view: View?) {
        setButton(AlertDialog.BUTTON_POSITIVE,
                context.getString(R.string.send)
        ) { _, _ ->
            onClickSendEmail(eTextMail.text.toString())

            eTextMail.hideSoftInput()
        }

        setButton(AlertDialog.BUTTON_NEGATIVE,
                context.getString(R.string.cancel)
        ) { _, _ -> eTextMail.hideSoftInput() }

        setOnShowListener {
            eTextMail.showSoftInput()

            val positiveButton = getButton(AlertDialog.BUTTON_POSITIVE)

            positiveButton.isEnabled = false

            eTextMail.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    positiveButton.isEnabled = s
                            .toString()
                            .trim()
                            .split("\\s+".toRegex())
                            .size > 2
                }

                override fun afterTextChanged(s: Editable) {

                }
            })
        }

        setOnCancelListener { eTextMail.hideSoftInput() }
        setOnDismissListener { eTextMail.hideSoftInput() }
    }

    private fun onClickSendEmail(content: String) {
        val intent = Intent(Intent.ACTION_SEND)
                .setType("message/rfc822")
                .putExtra(Intent.EXTRA_TEXT, content)
                .putExtra(Intent.EXTRA_SUBJECT, Constants.CONTACT_SUBJECT)
                .putExtra(Intent.EXTRA_EMAIL, arrayOf(Constants.CONTACT_EMAIL))

        try {
            context.startActivity(
                    Intent.createChooser(intent,
                            context.getString(R.string.aboutSendEmail)))
        } catch (exception: android.content.ActivityNotFoundException) {
            exception.printStackTrace()

            Toast.makeText(context,
                    R.string.aboutNoEmailClient,
                    Toast.LENGTH_SHORT).show()
        }

    }
}