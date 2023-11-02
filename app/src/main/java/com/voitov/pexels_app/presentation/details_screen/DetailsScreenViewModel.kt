package com.voitov.pexels_app.presentation.details_screen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.voitov.pexels_app.R
import com.voitov.pexels_app.domain.AppMainSections
import com.voitov.pexels_app.domain.usecase.BookmarkInteractor
import com.voitov.pexels_app.domain.usecase.DownloadPhotoUseCase
import com.voitov.pexels_app.domain.usecase.GetPhotoDetailsUseCase
import com.voitov.pexels_app.navigation.AppNavScreen
import com.voitov.pexels_app.presentation.BaseViewModel
import com.voitov.pexels_app.presentation.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsScreenViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getPhotoDetailsUseCase: GetPhotoDetailsUseCase,
    private val downloadPhotoUseCase: DownloadPhotoUseCase,
    private val interactor: BookmarkInteractor,
) :
    BaseViewModel<DetailsScreenSideEffect, DetailsScreenUiState, DetailsEvent>(
        DetailsScreenUiState.Loading(showError = false)
    ) {
    private var sourceScreen: AppMainSections =
        AppMainSections.valueOf(requireNotNull(savedStateHandle[AppNavScreen.DetailsScreen.SOURCE_SCREEN_PARAM]))


    private val photoId: Int =
        requireNotNull(savedStateHandle[AppNavScreen.DetailsScreen.PHOTO_ID_PARAM])

    private val query: String =
        requireNotNull(savedStateHandle[AppNavScreen.DetailsScreen.QUERY])

    init {
        retrievePhoto()
    }

    private fun retrievePhoto() {
        viewModelScope.launch {
            try {
                val details = getPhotoDetailsUseCase(sourceScreen, photoId)
                Log.d(TAG, details.toString())
                updateState(DetailsScreenUiState.Success(details))
            } catch (ex: Exception) {
                updateState(DetailsScreenUiState.Failure)
            }
        }
    }

    override fun onEvent(event: DetailsEvent) {
        when (event) {
            DetailsEvent.OnBookmarkPhoto -> handleOnBookmarkPhoto()
            DetailsEvent.OnDownloadPhoto -> handleOnDownloadPhoto()
            DetailsEvent.OnExplore -> handleOnExplore()
            DetailsEvent.OnNavigateBack -> handleOnNavigateBack()
            DetailsEvent.OnLoadingImageFailed -> handleLoadingImageFailed()
        }
    }

    private fun handleOnBookmarkPhoto() {
        try {
            val state = _state.value
            require(state is DetailsScreenUiState.Success)
            viewModelScope.launch {
                interactor(photoId, state.details, query)
                retrievePhoto()
            }
        } catch (_: Exception) {
            sendSideEffect(DetailsScreenSideEffect.ShowToast(UiText.Resource(R.string.failed_change_bookmark)))
        }
    }

    private fun handleOnDownloadPhoto() {
        viewModelScope.launch {
            val photoDetails = _state.value
            require(photoDetails is DetailsScreenUiState.Success)
            val result = downloadPhotoUseCase(photoDetails.details)

            sendSideEffect(
                DetailsScreenSideEffect.ShowToast(
                    if (result.isFailure) {
                        UiText.Resource(R.string.download_failed)
                    } else {
                        UiText.Resource(R.string.download_succeed)
                    }
                )
            )
        }
    }

    private fun handleOnExplore() {
        sendSideEffect(DetailsScreenSideEffect.NavigateToHomeScreen)
    }

    private fun handleOnNavigateBack() {
        sendSideEffect(DetailsScreenSideEffect.NavigateToPreviousScreen)
    }

    private fun handleLoadingImageFailed() {
        updateState(DetailsScreenUiState.Failure)
    }

    companion object {
        private const val TAG = "DetailsScreenViewModel"
    }
}