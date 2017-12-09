package com.gdavidpb.tuindice.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.gdavidpb.tuindice.*
import com.gdavidpb.tuindice.abstracts.Initializer
import com.gdavidpb.tuindice.models.DstService
import com.gdavidpb.tuindice.models.database
import com.gdavidpb.tuindice.models.preferences
import com.gdavidpb.tuindice.tabs.DataTab
import com.gdavidpb.tuindice.tabs.SummaryTab
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity(), Initializer {

    private var account = DstAccount()

    private lateinit var dataTab: DataTab
    private lateinit var summaryTab: SummaryTab

    /* Overrides */
    override fun onInitialize(view: View?) {
        /* Set up activity */
        setContentView(R.layout.activity_main)

        /* Stop login service */
        DstService.stopService(applicationContext)

        /* Setup tab host */
        dataTab = DataTab()
        summaryTab = SummaryTab()

        tabHost.setUp(supportFragmentManager, {
            addTab(R.string.tabData, dataTab)
            addTab(R.string.tabSummary, summaryTab)
        })

        supportActionBar?.title = getString(R.string.app_title,
                account.careerCode,
                account.careerName)

        /* Start demo activity if it's the first run */
        if (preferences.getFirstRun())
            startActivity<DemoActivity>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            /* Get active account from database */
            account = database.getActiveAccount()

            if (account.isEmpty()) {
                startActivity<LoginActivity>()
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
        database.close()

        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_about -> {
            startActivity<AboutActivity>()
            true
        }
        R.id.action_demo -> {
            startActivity<DemoActivity>()
            true
        }
        R.id.action_report -> {
            onContact( )
            true
        }
        R.id.action_exit -> {
           onLogout()
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun onLogout() {
        alert(R.string.alertMessageExit,
                R.string.alertTitleExit) {
            positiveButton(R.string.exit) {
                deleteReport()

                database.removeActiveAccount()
                database.removeTemporaryAccount()

                recreate()
            }
            negativeButton(R.string.cancel) { }
        }.show()
    }

    private fun onCantInitialize() {
        alert(R.string.alertMessageCantInit,
                R.string.alertTitleCantInit) {
            isCancelable = false

            positiveButton(R.string.exit) {
                finish()
                System.exit(0)
            }
        }.show()
    }
}
