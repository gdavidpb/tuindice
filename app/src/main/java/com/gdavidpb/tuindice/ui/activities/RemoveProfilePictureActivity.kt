package com.gdavidpb.tuindice.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gdavidpb.tuindice.utils.Extras

class RemoveProfilePictureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resultIntent = Intent().putExtra(Extras.REMOVE_PROFILE_PICTURE, true)

        setResult(Activity.RESULT_OK, resultIntent)

        finish()
    }
}