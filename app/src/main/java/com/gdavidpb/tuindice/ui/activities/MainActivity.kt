package com.gdavidpb.tuindice.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.utils.notNull
import com.gdavidpb.tuindice.data.utils.observe
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.presentation.viewmodel.MainActivityViewModel
import com.gdavidpb.tuindice.ui.fragments.EnrollmentFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import org.jetbrains.anko.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val viewModel: MainActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)

        super.onCreate(savedInstanceState)

        with(viewModel) {
            observe(getActiveAccount, ::getActiveAccountObserver)
            observe(logout, ::logoutObserver)
            observe(fetchStartUpAction, ::startUpObserver)
            observe(signInWithLink, ::signInWithLinkObserver)

            fetchStartUpAction(intent)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START))
            drawer_layout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId

        when (itemId) {
            R.id.nav_share -> share(text = getString(R.string.aboutShareMessage, packageName))
            R.id.nav_twitter -> browse(url = getString(R.string.devTwitter))
            R.id.nav_contact -> email(email = getString(R.string.devEmail), subject = getString(R.string.devContactSubject))
            R.id.nav_sign_out -> logoutDialog()
            else -> loadFragment(itemId)
        }

        drawer_layout.closeDrawer(GravityCompat.START)

        return true
    }

    private fun onViewCreated() {
        setSupportActionBar(toolbar)

        ActionBarDrawerToggle(
                this, drawer_layout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close).let { toggle ->
            drawer_layout.addDrawerListener(toggle)

            toggle.syncState()
        }

        nav_view.setNavigationItemSelectedListener(this)

        arrayOf(R.id.nav_twitter, R.id.nav_contact)
                .forEach {
                    nav_view.menu.findItem(it).setActionView(R.layout.view_open)
                }
    }

    private fun logoutDialog() {
        alert {
            titleResource = R.string.alertTitleLogout
            messageResource = R.string.alertMessageLogout

            positiveButton(R.string.yes) {
                viewModel.logout()
            }

            negativeButton(R.string.cancel) { }
        }.show()
    }

    private fun fatalFailureDialog() {
        alert {
            titleResource = R.string.alertTitleFatalFailure
            messageResource = R.string.alertMessageFatalFailure

            isCancelable = false

            positiveButton(R.string.settings) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", packageName, null))

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                startActivity(intent)

                finish()
            }

            negativeButton(R.string.exit) {
                finish()
            }
        }.show()
    }

    private fun loadAccount(account: Account) {
        val header = nav_view.getHeaderView(0)

        with(header) {
            tViewDrawerName.text = account.shortName
            tViewDrawerUsbId.text = account.email
        }
    }

    private fun loadFragment(@IdRes itemId: Int) {
        if (nav_view.checkedItem == null)
            nav_view.setCheckedItem(itemId)

        // todo complete fragments
        when (itemId) {
            R.id.nav_enrollment -> R.string.nav_enrollment to EnrollmentFragment()
            R.id.nav_record -> R.string.nav_record to Fragment()
            R.id.nav_calendar -> R.string.nav_calendar to Fragment()
            R.id.nav_pensum -> R.string.nav_pensum to Fragment()
            R.id.nav_achievements -> R.string.nav_achievements to Fragment()
            R.id.nav_podium -> R.string.nav_podium to Fragment()
            R.id.nav_about -> R.string.nav_about to Fragment()
            else -> null
        }.notNull { (title, fragment) ->
            supportActionBar?.setTitle(title)

            supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.content_fragment, fragment)
                    .commit()
        }
    }

    private fun signInWithLinkObserver(result: Completable?) {
        when (result) {
            is Completable.OnComplete -> {
                viewModel.getActiveAccount(tryRefresh = false)
            }
            is Completable.OnError -> {
                //todo fail to open auth link
            }
        }
    }

    private fun getActiveAccountObserver(result: Result<Account>?) {
        when (result) {
            is Result.OnSuccess -> {
                val account = result.value

                longToast(getString(R.string.toastWelcome, account.shortName))

                setContentView(R.layout.activity_main)

                onViewCreated()

                loadFragment(R.id.nav_enrollment)

                loadAccount(account = account)
            }
            is Result.OnError -> {
                fatalFailureDialog()
            }
        }
    }

    private fun logoutObserver(result: Completable?) {
        when (result) {
            is Completable.OnComplete -> {
                startActivity<LoginActivity>()
                finish()
            }
            is Completable.OnError -> {
                fatalFailureDialog()
            }
        }
    }

    private fun startUpObserver(result: Result<StartUpAction>?) {
        when (result) {
            is Result.OnSuccess -> {
                when (result.value) {
                    StartUpAction.MAIN -> {
                        viewModel.getActiveAccount(tryRefresh = false)
                    }
                    StartUpAction.EMAIL_SENT -> {
                        startActivity<EmailSentActivity>()
                        finish()
                    }
                    StartUpAction.EMAIL_LINK -> {
                        val emailLink = "${intent.data}"

                        viewModel.signInWithLink(link = emailLink)
                    }
                    StartUpAction.LOGIN -> {
                        startActivity<LoginActivity>()
                        finish()
                    }
                }
            }
            is Result.OnError -> {
                fatalFailureDialog()
            }
        }
    }
}
