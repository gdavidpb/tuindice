package com.gdavidpb.tuindice.migrations

import android.app.ActivityManager
import android.content.Context
import androidx.core.content.edit
import com.gdavidpb.tuindice.utils.extensions.awaitOrNull
import com.gdavidpb.tuindice.utils.extensions.sharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class ApiMigration(
	private val context: Context
) : Migration() {

	private val preferences by lazy {
		context.sharedPreferences()
	}

	private val firebaseAuth by lazy {
		FirebaseAuth.getInstance()
	}

	private val firebaseFirestore by lazy {
		FirebaseFirestore.getInstance()
	}

	override fun isRequired(): Boolean {
		val isMigrated = preferences
			.getBoolean("ApiMigration", false)

		if (isMigrated) return false

		val signInProvider = runBlocking {
			firebaseAuth
				.currentUser
				?.getIdToken(false)
				?.awaitOrNull()
				?.signInProvider
		}

		return (signInProvider != "custom")
	}

	override fun onFailure(throwable: Throwable) {
		val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

		activityManager.clearApplicationUserData()
	}

	override fun onMigrate() {
		runBlocking {
			firebaseAuth.signOut()
			firebaseFirestore.terminate().await()
			firebaseFirestore.clearPersistence().await()
		}

		preferences.edit {
			putBoolean("ApiMigration", true)
		}
	}
}