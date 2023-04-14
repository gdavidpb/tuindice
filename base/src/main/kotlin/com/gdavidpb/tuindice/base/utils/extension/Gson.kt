package com.gdavidpb.tuindice.base.utils.extension

import com.google.gson.Gson

inline fun <reified T : Any> Gson.fromJson(json: String): T =
	fromJson(json, T::class.java)