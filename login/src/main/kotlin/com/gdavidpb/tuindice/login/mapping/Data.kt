package com.gdavidpb.tuindice.login.mapping

import okhttp3.Credentials

fun encodeCredentialsBasic(username: String, password: String) =
	Credentials.basic(username, password)