package com.example.movieguide.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieguide.data.local.AppDatabase
import com.example.movieguide.data.local.FavoriteMovie
import com.example.movieguide.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class FavoritesUiState {
    object Loading : FavoritesUiState()
    data class Success(val favorites: List<FavoriteMovie>) : FavoritesUiState()
    data class Error(val message: String) : FavoritesUiState()
    object Empty : FavoritesUiState()
}

class FavoritesViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = FavoritesRepository(database)

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        _uiState.value = FavoritesUiState.Loading
        repository.getAllFavorites()
            .onEach { favorites ->
                if (favorites.isEmpty()) {
                    _uiState.value = FavoritesUiState.Empty
                } else {
                    _uiState.value = FavoritesUiState.Success(favorites)
                }
            }
            .launchIn(viewModelScope)
    }

    fun removeFavorite(movieId: Int) {
        viewModelScope.launch {
            repository.removeFromFavorites(movieId)
        }
    }
}

