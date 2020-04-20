package com.gdavidpb.tuindice.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gdavidpb.tuindice.utils.EXTRA_REMOVE_PROFILE_PICTURE

class RemoveProfilePictureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resultIntent = Intent().putExtra(EXTRA_REMOVE_PROFILE_PICTURE, true)

        setResult(Activity.RESULT_OK, resultIntent)

        finish()
    }
}