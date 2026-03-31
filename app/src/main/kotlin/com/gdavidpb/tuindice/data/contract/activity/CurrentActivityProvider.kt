package com.gdavidpb.tuindice.data.contract.activity

import android.app.Activity

interface CurrentActivityProvider {
	fun get(): Activity?
	fun set(activity: Activity)
}
