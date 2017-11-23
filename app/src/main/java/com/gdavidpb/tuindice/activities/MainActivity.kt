package com.gdavidpb.tuindice.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.content.res.AppCompatResources
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.crashlytics.android.Crashlytics
import com.gdavidpb.tuindice.*
import com.gdavidpb.tuindice.abstracts.Initializer
import com.gdavidpb.tuindice.models.DstService
import com.gdavidpb.tuindice.tabs.DataTab
import com.gdavidpb.tuindice.tabs.SummaryTab
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), Initializer {

    private var account = DstAccount()

    private val dataTab = DataTab()
    private val summaryTab = SummaryTab()

    /* Overrides */
    override fun onInitialize(view: View?) {
        /* Set up activity */
        setContentView(R.layout.activity_main)

        /* Stop login service */
        DstService.stopService(applicationContext)

        /* Setup tab host */
        tabHost.setUp(supportFragmentManager, {
            addTab(R.string.tabData, dataTab)
            addTab(R.string.tabSummary, summaryTab)
        })

        /* Set up drawables */
        val drawableAdd = AppCompatResources.getDrawable(this, R.drawable.ic_add)

        floatingAction.setImageDrawable(drawableAdd)

        /* Check for updates */
        val now = Calendar.getInstance()
        val lastUpdate = Calendar.getInstance()

        lastUpdate.timeInMillis = account.lastUpdate

        if (lastUpdate.monthsTo(now) >= 3) {
            /* Set up service */
            async( /* on Task */ {
                try {
                    DstService().collectFrom(account.usbId, account.password)
                } catch (exception: Exception) {
                    exception.printStackTrace()

                    DstResponse<DstAccount>(exception)
                }
            }, /* on Response */ {
                account = result!!

                getDatabase().addAccount(account, true)

                onInitialize()
            })
        }

        /* Reload from database */
        if (account.isOutdated())
            account = getDatabase().getActiveAccount()

        /* Update user interface */
        val bundle = Bundle()

        bundle.putSerializable(Constants.EXTRA_ACCOUNT, account)

        dataTab.update(bundle)
        summaryTab.update(bundle)

        supportActionBar?.title = getString(R.string.app_title,
                account.careerCode,
                account.careerName)

        /* Set up listeners */
        floatingAction.setOnClickListener { onAddClick() }

        /* Launch demo activity if it's the first run */
        if (getPreferences().getFirstRun())
            launchActivity<DemoActivity>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Set Spanish-Venezuela as default locale */
        Locale.setDefault(Locale("es", "VE"))

        /* Set up vector compatibility */
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        /* Set up Google Fabric */
        if (!BuildConfig.DEBUG)
            Fabric.with(this, Crashlytics())

        try {
            /* Get active account from database */
            account = getDatabase().getActiveAccount()

            if (account.isEmpty()) {
                launchActivity<LoginActivity>()
                finish()
            } else
                onInitialize()

        } catch (exception: Exception) {
            exception.printStackTrace()

            onCantInitialize()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        this.intent = intent
    }

    override fun onDestroy() {
        getDatabase().close()

        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_about -> {
            launchActivity<AboutActivity>()

            true
        }
        R.id.action_demo -> {
            launchActivity<DemoActivity>()
            true
        }
        R.id.action_exit -> {
            alertDialog {
                setTitle(R.string.alertTitleExit)
                setMessage(R.string.alertMessageExit)
                setPositiveButton(R.string.exit, { _, _ ->
                    getDatabase().removeActiveAccount()
                    getDatabase().removeTemporaryAccount()

                    recreate()
                })
                setNegativeButton(R.string.cancel, null)
            }

            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    /* Methods */
    private fun onCantInitialize() {
        alertDialog {
            setCancelable(false)
            setTitle(R.string.alertTitleCantInit)
            setMessage(R.string.alertMessageCantInit)
            setPositiveButton(R.string.exit, { _, _ ->
                finish()
                System.exit(0)
            })
        }
    }

    private fun onAddClick() {

    }
}
