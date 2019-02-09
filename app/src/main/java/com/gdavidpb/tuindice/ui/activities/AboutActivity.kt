package com.gdavidpb.tuindice.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gdavidpb.tuindice.R

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()

        return true
    }
}
