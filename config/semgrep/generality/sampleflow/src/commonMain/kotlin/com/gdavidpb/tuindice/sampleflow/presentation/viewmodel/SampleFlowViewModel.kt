package com.gdavidpb.tuindice.sampleflow.presentation.viewmodel

import android.util.Log
import java.io.File
import org.koin.androidx.viewmodel.dsl.viewModelOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.gdavidpb.tuindice.sampleflow.domain.usecase.LoadSampleUseCase
import com.gdavidpb.tuindice.sampleflow.data.source.SampleFlowDataSource
import com.gdavidpb.tuindice.sampleflow.domain.repository.SampleFlowRepository
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class SampleFlowViewModel : ViewModel() {
	private val uiState = MutableStateFlow(0)

	fun refreshAction() {
		viewModelScope.launch { }
	}

	fun closeAction() {
		sendEffect(Unit)
	}

	fun retryAction() {
		val snapshot = currentState
		Log.d("sampleflow", snapshot.toString())
	}

	fun versionAction(): String = BuildConfig.VERSION_NAME
}
