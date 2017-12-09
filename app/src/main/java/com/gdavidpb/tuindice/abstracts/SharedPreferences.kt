package com.gdavidpb.tuindice.abstracts

import android.annotation.SuppressLint
import android.content.SharedPreferences

abstract class SharedPreferences(private val delegate: SharedPreferences) : SharedPreferences {

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        delegate.unregisterOnSharedPreferenceChangeListener(listener)
    }

    override fun getBoolean(key: String, defValue: Boolean) = delegate.getBoolean(key, defValue)

    override fun getInt(key: String, defValue: Int) = delegate.getInt(key, defValue)

    override fun getAll(): MutableMap<String, *> = delegate.all

    override fun getLong(key: String, defValue: Long) = delegate.getLong(key, defValue)

    override fun getFloat(key: String, defValue: Float) = delegate.getFloat(key, defValue)

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? = delegate.getStringSet(key, defValues)

    override fun getString(key: String, defValue: String?): String? = delegate.getString(key, defValue)

    override fun contains(key: String) = delegate.contains(key)

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        delegate.registerOnSharedPreferenceChangeListener(listener)
    }

    @SuppressLint("CommitPrefEdits")
    override fun edit() = Editor(delegate.edit())

    class Editor(private val delegate: SharedPreferences.Editor) : SharedPreferences.Editor {

        /* Inline editor */
        private inline fun edit(edition: () -> Unit) : Editor {
            edition()
            apply()
            return this
        }

        override fun putLong(key: String, value: Long) = edit { delegate.putLong(key, value) }

        override fun putInt(key: String, value: Int) = edit { delegate.putInt(key, value) }

        override fun putBoolean(key: String, value: Boolean) = edit { delegate.putBoolean(key, value) }

        override fun putStringSet(key: String, values: MutableSet<String>?) = edit { delegate.putStringSet(key, values) }

        override fun putString(key: String, value: String?) = edit { delegate.putString(key, value) }

        override fun putFloat(key: String, value: Float) = edit { delegate.putFloat(key, value) }

        override fun remove(key: String) = edit { delegate.remove(key) }

        override fun clear() = edit { delegate.clear() }

        override fun commit() = delegate.commit()

        override fun apply() {
            delegate.apply()
        }
    }
}