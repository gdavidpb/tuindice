package com.gdavidpb.tuindice.data.source.settings

import android.content.SharedPreferences
import com.gdavidpb.tuindice.data.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.data.utils.edit
import com.gdavidpb.tuindice.domain.repository.SettingsRepository
import io.reactivex.Completable
import io.reactivex.Single
import java.util.*

open class PreferencesDataStore(
        private val preferences: SharedPreferences
) : SettingsRepository {
    /* Preferences keys */
    private val keyFirstRun = "firstRun"
    private val keyCooldown = "cooldown"
    private val emailSentTo = "getEmailSentTo"

    override fun clearEmailSentTo(): Completable {
        return Completable.fromCallable {
            preferences.edit {
                remove(emailSentTo)
            }
        }
    }

    override fun getEmailSentTo(): Single<String> {
        return Single.fromCallable {
            preferences.getString(emailSentTo, "")
        }
    }

    override fun setEmailSentTo(email: String): Completable {
        return Completable.fromCallable {
            preferences.edit {
                putString(emailSentTo, email)
            }
        }
    }

    override fun setCooldown(key: String): Completable {
        return Completable.fromCallable {
            val calendar = Calendar.getInstance(DEFAULT_LOCALE)

            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            calendar.add(Calendar.DATE, 1)

            preferences.edit {
                putLong("$key$keyCooldown", calendar.timeInMillis)
            }
        }
    }

    override fun isCooldown(key: String): Single<Boolean> {
        return Single.fromCallable {
            val now = Calendar.getInstance(DEFAULT_LOCALE)
            val cooldown = Calendar.getInstance(DEFAULT_LOCALE)

            cooldown.timeInMillis = preferences.getLong("$key$keyCooldown", now.timeInMillis)

            now.before(cooldown)
        }
    }

    override fun isFirstRun(): Single<Boolean> {
        return Single.fromCallable {
            preferences.getBoolean(keyFirstRun, true)
        }
    }

    override fun setFirstRun(): Completable {
        return Completable.fromCallable {
            preferences.edit {
                putBoolean(keyFirstRun, true)
            }
        }
    }

    override fun clear(): Completable {
        return Completable.fromCallable {
            preferences.edit {
                clear()
            }
        }
    }
}