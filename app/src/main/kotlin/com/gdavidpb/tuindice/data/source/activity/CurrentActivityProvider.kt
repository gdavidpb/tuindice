package com.gdavidpb.tuindice.data.source.activity

import android.app.Activity

interface CurrentActivityProvider {
	fun get(): Activity?
	fun set(activity: Activity)
}
