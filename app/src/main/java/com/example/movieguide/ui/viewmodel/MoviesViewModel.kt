package com.example.movieguide.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieguide.data.model.Movie
import com.example.movieguide.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MoviesUiState {
    object Loading : MoviesUiState()
    data class Success(val movies: List<Movie>) : MoviesUiState()
    data class Error(val message: String) : MoviesUiState()
}

class MoviesViewModel(
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<MoviesUiState>(MoviesUiState.Loading)
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    init {
        loadMovies()
    }

    fun loadMovies() {
        viewModelScope.launch {
            _uiState.value = MoviesUiState.Loading
            repository.getPopularMovies()
                .onSuccess { movies ->
                    _uiState.value = MoviesUiState.Success(movies)
                }
                .onFailure { exception ->
                    _uiState.value = MoviesUiState.Error(exception.message ?: "Unknown error")
                }
        }
    }
}

