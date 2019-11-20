package com.gdavidpb.tuindice.ui.activities

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.core.view.get
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.usecase.coroutines.Result
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        with(viewModel) {
            observe(fetchStartUpAction, ::startUpObserver)

            fetchStartUpAction(dataString = intent.dataString ?: "")
        }
    }

    private fun initView(@IdRes navId: Int) {
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)

        val appBarItems = setOf(
                R.id.navigation_summary,
                R.id.navigation_record,
                R.id.navigation_about
        )

        val appBarConfiguration = AppBarConfiguration(appBarItems)

        setupActionBarWithNavController(navController, appBarConfiguration)

        nav_view.setupWithNavController(navController)

        navController.navigate(navId)

        nav_view.menu[appBarItems.indexOf(navId)].isChecked = true

        nav_view.setOnNavigationItemSelectedListener { item ->
            if (nav_view.selectedItemId != item.itemId) {
                val newNavId = item.itemId.toNavId()

                viewModel.setLastScreen(newNavId)

                navController.navigate(newNavId)

                true
            } else
                false
        }
    }

    private fun fatalFailureDialog() {
        alert {
            titleResource = R.string.alert_title_fatal_failure
            messageResource = R.string.alert_message_fatal_failure

            isCancelable = false

            positiveButton(R.string.settings) {
                openSettings()

                finish()
            }

            negativeButton(R.string.exit) {
                finish()
            }
        }.show()
    }

    private fun startUpObserver(result: Result<StartUpAction>?) {
        when (result) {
            is Result.OnSuccess -> {
                when (val value = result.value) {
                    is StartUpAction.Main -> {
                        initView(navId = value.screen)
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
