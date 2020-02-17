package com.gdavidpb.tuindice.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.utils.extensions.*
import com.google.android.material.snackbar.Snackbar
import org.jetbrains.anko.contentView

@SuppressLint("Registered")
abstract class BaseActivity(private vararg val permissions: String) : AppCompatActivity() {

    protected val snackBar by lazy {
        Snackbar.make(contentView ?: window.decorView, "", Snackbar.LENGTH_LONG)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (permissions.isNotEmpty())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(*permissions)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val granted = grantResults.all {
            it == PackageManager.PERMISSION_GRANTED
        }

        val denied = grantResults.all {
            it == PackageManager.PERMISSION_DENIED
        }

        if (!granted || denied)
            showDeniedAlert(denied)
    }

    fun checkPermissions(vararg permissions: String, exec: () -> Unit = { }): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionsGranted = permissions.all {
                checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            }

            if (permissionsGranted)
                exec()
            else
                showDeniedAlert(true)

            permissionsGranted
        } else {
            exec()

            true
        }
    }

    private fun showDeniedAlert(denied: Boolean) {
        alert {
            isCancelable = false

            titleResource = R.string.alert_title_permissions
            messageResource = R.string.alert_message_permissions

            if (denied) {
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
            } else {
                positiveButton(R.string.retry) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        this@BaseActivity.requestPermissions(*permissions)
                }

                negativeButton(R.string.exit) {
                    finish()
                }
            }
        }.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermissions(vararg permissions: String) {
        val permissionsGranted = permissions.all {
            checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }

        if (!permissionsGranted) {
            val remainingPermissions = permissions.filter {
                checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
            }.toTypedArray()

            /* Default request code */
            requestPermissions(remainingPermissions, 0)
        }
    }
}