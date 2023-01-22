package com.gdavidpb.tuindice.summary.ui.fragments

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.domain.usecase.base.Completable
import com.gdavidpb.tuindice.base.domain.usecase.base.Event
import com.gdavidpb.tuindice.base.domain.usecase.base.Result
import com.gdavidpb.tuindice.base.ui.dialogs.ConfirmationBottomSheetDialog
import com.gdavidpb.tuindice.base.ui.fragments.NavigationFragment
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.domain.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.mapping.formatLastUpdate
import com.gdavidpb.tuindice.summary.mapping.toCreditsSummaryItem
import com.gdavidpb.tuindice.summary.mapping.toShortName
import com.gdavidpb.tuindice.summary.mapping.toSubjectsSummaryItem
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.adapters.SummaryAdapter
import com.gdavidpb.tuindice.summary.ui.manager.ProfilePictureManager
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.fragment_summary.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SummaryFragment : NavigationFragment() {

	private val viewModel by viewModel<SummaryViewModel>()

	private val profilePictureManager by inject<ProfilePictureManager>()

	private val picasso by inject<Picasso>()

	private val summaryAdapter = SummaryAdapter()

	private val activityLauncher = registerForActivityResult(
		profilePictureManager.provideActivityResultContracts(),
		profilePictureManager.provideActivityResultCallback()
	)

	override fun onCreateView() = R.layout.fragment_summary

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		rViewSummary.adapter = summaryAdapter

		vProfilePicture.onClickOnce(::onEditProfilePictureClick)

		with(requireActivity()) {
			profilePictureManager.init(activityLauncher, ProfilePictureManagerListener())
			addMenuProvider(SummaryMenuProvider(), viewLifecycleOwner)
		}

		viewModel.getAccount()
	}

	override fun onInitObservers() {
		with(viewModel) {
			observe(signOut, ::signOutObserver)
			observe(account, ::accountObserver)
			observe(uploadProfilePicture, ::uploadProfilePictureObserver)
			observe(removeProfilePicture, ::removeProfilePictureObserver)
		}
	}

	private fun onEditProfilePictureClick() {
		profilePictureManager.pickPicture(
			activity = requireActivity(),
			includeRemove = vProfilePicture.hasProfilePicture
		)
	}

	private fun loadProfile(account: Account) {
		val context = requireContext()

		/* Load account */

		val shortName = account.toShortName()
		val lastUpdate =
			context.getString(R.string.text_last_update, account.lastUpdate.formatLastUpdate())

		tViewName.text = shortName
		tViewCareer.text = account.careerName

		tViewLastUpdate.text = lastUpdate
		tViewLastUpdate.isVisible = true

		if (account.grade > 0.0) {
			tViewGrade.isVisible = true
			tViewGrade.animateGrade(value = account.grade.toFloat())
		} else
			tViewGrade.isVisible = false

		loadProfilePictureFromUrl(url = account.pictureUrl)

		/* Load summary */

		val subjectsSummary = account.toSubjectsSummaryItem(context)
		val creditsSummary = account.toCreditsSummaryItem(context)

		val items = listOf(subjectsSummary, creditsSummary)

		summaryAdapter.submitSummary(items)
	}

	private fun loadProfilePictureFromUrl(url: String?) {
		if (url == null) {
			vProfilePicture.setLoading(false)
			vProfilePicture.setDrawable(null)

			return
		}

		with(picasso) {
			invalidate(url)

			load(url)
				.noFade()
				.stableKey(url)
				.into(object : Target {
					override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
						val drawable = bitmap.toDrawable(resources)

						vProfilePicture.setLoading(false)
						vProfilePicture.setDrawable(drawable)
					}

					override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
						vProfilePicture.setLoading(false)
						errorSnackBar()
					}

					override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
						vProfilePicture.setLoading(true)
					}
				})
		}
	}

	private fun navigateToSignIn() {
		findNavController().popStackToRoot()

		navigate(SummaryFragmentDirections.navToSignIn())
	}

	private fun signOutObserver(result: Completable<Nothing>?) {
		when (result) {
			is Completable.OnComplete -> {
				navigateToSignIn()
			}
			is Completable.OnError -> {
				requireActivity().recreate()
			}
			else -> {}
		}
	}

	private fun accountObserver(result: Result<Account, Nothing>?) {
		when (result) {
			is Result.OnSuccess -> loadProfile(account = result.value)
			is Result.OnError -> {
				// TODO error handler
				//tViewLastUpdate.drawables(start = R.drawable.ic_sync_problem)
				//tViewLastUpdate.onClickOnce { snackBar(R.string.snack_no_service) }
			}
			else -> {}
		}
	}

	private fun uploadProfilePictureObserver(result: Event<String, ProfilePictureError>?) {
		when (result) {
			is Event.OnLoading -> vProfilePicture.setLoading(true)
			is Event.OnSuccess -> {
				loadProfilePictureFromUrl(url = result.value)

				snackBar(R.string.snack_profile_picture_updated)
			}
			is Event.OnError -> {
				vProfilePicture.setLoading(false)

				profilePictureErrorHandler(error = result.error)
			}
			else -> {}
		}
	}

	private fun removeProfilePictureObserver(result: Event<Unit, ProfilePictureError>?) {
		when (result) {
			is Event.OnLoading -> vProfilePicture.setLoading(true)
			is Event.OnSuccess -> {
				loadProfilePictureFromUrl(null)

				snackBar(R.string.snack_profile_picture_removed)
			}
			is Event.OnError -> {
				vProfilePicture.setLoading(false)

				profilePictureErrorHandler(error = result.error)
			}
			else -> {}
		}
	}

	private fun profilePictureErrorHandler(error: ProfilePictureError?) {
		when (error) {
			is ProfilePictureError.Timeout -> errorSnackBar(R.string.snack_timeout)
			is ProfilePictureError.NoData -> vProfilePicture.setDrawable(null)
			is ProfilePictureError.NoConnection -> connectionSnackBar(error.isNetworkAvailable)
			else -> errorSnackBar()
		}
	}

	inner class SummaryMenuProvider : MenuProvider {
		override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
			menuInflater.inflate(R.menu.menu_summary, menu)
		}

		override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
			return when (menuItem.itemId) {
				R.id.menu_sign_out -> {
					bottomSheetDialog<ConfirmationBottomSheetDialog> {
						titleResource = R.string.dialog_title_sign_out
						messageResource = R.string.dialog_message_sign_out

						positiveButton(R.string.menu_sign_out) { viewModel.signOut() }
						negativeButton(R.string.cancel)
					}

					true
				}
				else -> false
			}
		}
	}

	inner class ProfilePictureManagerListener : ProfilePictureManager.ProfilePictureListener {
		override fun onProfilePictureSelected(uri: Uri?) {
			uri ?: return

			viewModel.uploadProfilePicture(path = "$uri")
		}

		override fun onProfilePictureRemoved() {
			bottomSheetDialog<ConfirmationBottomSheetDialog> {
				titleResource = R.string.dialog_title_remove_profile_picture_failure
				messageResource = R.string.dialog_message_remove_profile_picture_failure

				positiveButton(R.string.remove) { viewModel.removeProfilePicture() }
				negativeButton(R.string.cancel)
			}
		}

		override fun onProfilePictureError() {
			errorSnackBar()
		}
	}
}