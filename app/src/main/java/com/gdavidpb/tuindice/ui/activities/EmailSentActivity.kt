package com.gdavidpb.tuindice.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.utils.observe
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.presentation.viewmodel.EmailSentActivityViewModel
import kotlinx.android.synthetic.main.activity_email_sent.*
import org.jetbrains.anko.startActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class EmailSentActivity : AppCompatActivity() {

    private val viewModel: EmailSentActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_sent)

        with(viewModel) {
            observe(getEmailSentTo, ::getEmailSentToObserver)

            getEmailSentTo()
        }
    }

    private fun getEmailSentToObserver(result: Result<String>?) {
        when (result) {
            is Result.OnSuccess -> {
                // todo style email, redesign this activity
                tViewEmailSent.text = getString(R.string.messageEmailSent, result.value)
            }
            is Result.OnEmpty, is Result.OnError -> {
                startActivity<MainActivity>()
                finish()
            }
        }
    }
}
