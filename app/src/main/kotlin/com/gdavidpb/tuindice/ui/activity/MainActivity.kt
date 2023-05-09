package com.gdavidpb.tuindice.ui.activity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.base.utils.IdempotentLocker
import com.gdavidpb.tuindice.base.utils.RequestCodes
import com.gdavidpb.tuindice.base.utils.TIME_EXIT_LOCKER
import com.gdavidpb.tuindice.base.utils.extension.animateSlideIn
import com.gdavidpb.tuindice.base.utils.extension.animateSlideOut
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.base.utils.extension.hideSoftKeyboard
import com.gdavidpb.tuindice.base.utils.extension.launchRepeatOnLifecycle
import com.gdavidpb.tuindice.base.utils.extension.toast
import com.gdavidpb.tuindice.base.utils.extension.view
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

	private val appBar by view<AppBarLayout>(R.id.appBar)
	private val bottomNavView by view<BottomNavigationView>(R.id.bottomNavView)
	private val toolbar by view<MaterialToolbar>(R.id.toolbar)

	private val reviewManager by inject<ReviewManager>()

	private val updateManager by inject<AppUpdateManager>()

	private val viewModel by viewModel<MainViewModel>()

	private val backLocker = IdempotentLocker()

	private val topDestinations = setOf(
		R.id.fragment_summary,
		R.id.fragment_record,
		R.id.fragment_about,
		R.id.fragment_splash,
		R.id.fragment_sign_in
	)

	private val bottomDestinations = setOf(
		R.id.fragment_summary,
		R.id.fragment_record,
		R.id.fragment_about
	)

	private val appBarConfiguration by lazy {
		AppBarConfiguration(topDestinations)
	}

	private val navController by lazy {
		findNavController(R.id.mainNavHostFragment)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		setTheme(R.style.AppTheme)

		super.onCreate(savedInstanceState)

		setContentView(R.layout.activity_main)

		setSupportActionBar(toolbar)

		NavigationUI.setupWithNavController(bottomNavView, navController)
		NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration)

		bottomNavView.setOnItemReselectedListener { }

		navController.addOnDestinationChangedListener { _, destination, _ ->
			onDestinationChanged(destination)
		}

		onBackPressedDispatcher.addCallback(this, BackPressedHandler())

		launchRepeatOnLifecycle {
			with(viewModel) {
				collect(viewEvent, ::eventCollector)
			}
		}

		viewModel.requestReviewAction(reviewManager)
	}

	private fun eventCollector(event: Main.Event) {
		when (event) {
			is Main.Event.ShowReviewDialog ->
				showReviewDialog(reviewInfo = event.reviewInfo)

			is Main.Event.StartUpdateFlow ->
				updateManager.startUpdateFlowForResult(
					event.updateInfo,
					AppUpdateType.IMMEDIATE,
					this@MainActivity,
					RequestCodes.APP_UPDATE
				)
		}
	}

	override fun onResume() {
		super.onResume()

		viewModel.checkUpdateAction(updateManager)
	}

	private fun showReviewDialog(reviewInfo: ReviewInfo) {
		lifecycleScope.launchWhenResumed {
			reviewManager.launchReview(
				activity = this@MainActivity,
				reviewInfo = reviewInfo
			)
		}
	}

	private fun onDestinationChanged(destination: NavDestination) {
		hideSoftKeyboard()

		val showAppBar = (destination.id != R.id.fragment_splash)
		val showBottomNav = bottomDestinations.contains(destination.id)

		appBar.isVisible = showAppBar

		if (showBottomNav) {
			viewModel.setLastScreenAction(screen = destination.id)

			bottomNavView.animateSlideIn()
		} else {
			bottomNavView.animateSlideOut()
		}
	}

	inner class BackPressedHandler : OnBackPressedCallback(true) {
		override fun handleOnBackPressed() {
			val isHomeDestination = topDestinations.contains(navController.currentDestination?.id)

			if (isHomeDestination) {
				toast(R.string.toast_repeat_to_exit)

				val locked = backLocker.lock(UnlockIn = TIME_EXIT_LOCKER)

				if (!locked) finish()
			} else {
				navController.navigateUp()
			}
		}
	}
}