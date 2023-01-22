package com.gdavidpb.tuindice.summary.ui.manager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.utils.Actions
import com.gdavidpb.tuindice.summary.utils.Extras
import com.gdavidpb.tuindice.summary.utils.extensions.fileProviderUri
import java.io.File

class ProfilePictureManager {
	private var listener: ProfilePictureListener? = null
	private var register: ActivityResultLauncher<Intent>? = null

	private var cameraOutputUri: Uri? = null

	interface ProfilePictureListener {
		fun onProfilePictureSelected(uri: Uri?)
		fun onProfilePictureRemoved()
		fun onProfilePictureError()
	}

	fun init(register: ActivityResultLauncher<Intent>, listener: ProfilePictureListener) {
		this.register = register
		this.listener = listener
	}

	fun provideActivityResultContracts() = ActivityResultContracts.StartActivityForResult()

	fun provideActivityResultCallback() = ActivityResultCallback<ActivityResult> { result ->
		with(result) {
			if (resultCode == Activity.RESULT_OK) {
				val isRemoved = data?.hasExtra(Extras.REMOVE_PROFILE_PICTURE) ?: false
				val isPicked = data?.data != null
				val isTaken = cameraOutputUri != null

				when {
					isRemoved -> listener?.onProfilePictureRemoved()
					isPicked -> listener?.onProfilePictureSelected(uri = data?.data)
					isTaken -> listener?.onProfilePictureSelected(uri = cameraOutputUri)
				}
			}
		}
	}

	fun pickPicture(activity: FragmentActivity, includeRemove: Boolean) {
		val context = activity.applicationContext

		cameraOutputUri = createCameraOutputUri(context) ?: return

		val removeIntent = Intent(Actions.REMOVE_PROFILE_PICTURE)

		val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
			.putExtra(MediaStore.EXTRA_OUTPUT, cameraOutputUri)

		val galleryIntent = Intent(
			Intent.ACTION_PICK,
			MediaStore.Images.Media.EXTERNAL_CONTENT_URI
		)

		val chooser =
			Intent.createChooser(
				galleryIntent,
				context.getString(R.string.label_profile_picture_chooser)
			)

		val hasCamera = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

		val intents = mutableListOf<Intent>().apply {
			if (hasCamera) add(cameraIntent)
			if (includeRemove) add(removeIntent)
		}.toTypedArray()

		if (intents.isNotEmpty()) chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents)

		register?.launch(chooser)
	}

	private fun createCameraOutputUri(context: Context): Uri? {
		return runCatching {
			File(context.filesDir, "profile_picture.jpg")
				.run {
					if (exists()) delete()
					createNewFile()
					fileProviderUri(context)
				}
		}.onFailure {
			listener?.onProfilePictureError()
		}.getOrNull()
	}
}