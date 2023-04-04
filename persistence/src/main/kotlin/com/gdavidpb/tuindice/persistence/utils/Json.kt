package com.gdavidpb.tuindice.persistence.utils

import com.google.gson.Gson

internal fun <T : Any> T.toJson(gson: Gson): String =
	gson.toJson(this)

internal inline fun <reified T : Any> String.fromJson(gson: Gson): T =
	gson.fromJson(this, T::class.java)