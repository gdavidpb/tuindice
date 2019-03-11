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
import com.gdavidpb.tuindice.data.utils.CircleTransform
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.coroutines.Continuous
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.ui.fragments.*
import com.gdavidpb.tuindice.utils.*
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import org.jetbrains.anko.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val viewModel: MainViewModel by viewModel()

    private val picasso: Picasso by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)

        super.onCreate(savedInstanceState)

        with(viewModel) {
            observe(logout, ::logoutObserver)
            observe(account, ::accountObserver)
            observe(fetchStartUpAction, ::startUpObserver)

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
            R.id.nav_report -> reportDialog()
            R.id.nav_about -> startActivity<AboutActivity>()
            R.id.nav_sign_out -> logoutDialog()
            else -> loadFragment(itemId)
        }

        drawer_layout.closeDrawer(GravityCompat.START)

        return true
    }

    private fun initView() {
        setContentView(R.layout.activity_main)

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

        loadFragment(R.id.nav_summary)
    }

    private fun reportDialog() {
        val items = resources.getStringArray(R.array.selectorReportItems).toList()

        selector(getString(R.string.selectorTitleReport), items) { _, selected ->
            //todo show dialog, send to database
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
            tViewDrawerName.text = account.fullName.toShortName()
            tViewDrawerUsbId.text = account.email

            if (account.photoUrl.isNotEmpty())
                picasso.load(account.photoUrl).transform(CircleTransform()).into(iViewDrawerProfile)
            else
                iViewDrawerProfile.setImageResource(R.mipmap.ic_launcher_round)
        }
    }

    private fun loadFragment(@IdRes itemId: Int, init: ((Fragment) -> Fragment)? = null) {
        if (nav_view.checkedItem == null)
            nav_view.setCheckedItem(itemId)

        when (itemId) {
            R.id.nav_summary -> R.string.nav_summary to SummaryFragment()
            R.id.nav_record -> R.string.nav_record to RecordFragment()
            R.id.nav_calendar -> R.string.nav_calendar to CalendarFragment()
            R.id.nav_pensum -> R.string.nav_pensum to PensumFragment()
            R.id.nav_achievements -> R.string.nav_achievements to AchievementsFragment()
            R.id.nav_podium -> R.string.nav_podium to PodiumFragment()
            else -> null
        }.notNull { (title, fragment) ->
            supportActionBar?.setTitle(title)

            supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.content_fragment, init?.invoke(fragment) ?: fragment)
                    .commit()
        }
    }

    private fun accountObserver(result: Continuous<Account>?) {
        when (result) {
            is Continuous.OnNext -> {
                val account = result.value

                loadAccount(account = account)
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
                val value = result.value

                when (value) {
                    is StartUpAction.Main -> {
                        initView()
                    }
                    is StartUpAction.Reset -> {
                        startActivity<EmailSentActivity>(
                                EXTRA_AWAITING_STATE to FLAG_RESET,
                                EXTRA_AWAITING_EMAIL to value.email
                        )
                        finish()
                    }
                    is StartUpAction.Verify -> {
                        startActivity<EmailSentActivity>(
                                EXTRA_AWAITING_STATE to FLAG_VERIFY,
                                EXTRA_AWAITING_EMAIL to value.email
                        )
                        finish()
                    }
                    is StartUpAction.Login -> {
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
