package com.example.movieguide.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieguide.data.model.Genre
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

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGenre = MutableStateFlow<Genre?>(null)
    val selectedGenre: StateFlow<Genre?> = _selectedGenre.asStateFlow()

    init {
        loadMovies()
        loadGenres()
    }

    fun loadMovies() {
        viewModelScope.launch {
            _uiState.value = MoviesUiState.Loading
            _searchQuery.value = ""
            _selectedGenre.value = null
            repository.getPopularMovies()
                .onSuccess { movies ->
                    _uiState.value = MoviesUiState.Success(movies)
                }
                .onFailure { exception ->
                    _uiState.value = MoviesUiState.Error(exception.message ?: "Unknown error")
                }
        }
    }

    fun searchMovies(query: String) {
        if (query.isBlank()) {
            loadMovies()
            return
        }
        
        viewModelScope.launch {
            _uiState.value = MoviesUiState.Loading
            _searchQuery.value = query
            _selectedGenre.value = null
            repository.searchMovies(query)
                .onSuccess { movies ->
                    _uiState.value = MoviesUiState.Success(movies)
                }
                .onFailure { exception ->
                    _uiState.value = MoviesUiState.Error(exception.message ?: "Unknown error")
                }
        }
    }

    fun filterByGenre(genre: Genre?) {
        viewModelScope.launch {
            if (genre == null) {
                loadMovies()
                return@launch
            }
            
            _uiState.value = MoviesUiState.Loading
            _selectedGenre.value = genre
            _searchQuery.value = ""
            repository.getMoviesByGenre(genre.id)
                .onSuccess { movies ->
                    _uiState.value = MoviesUiState.Success(movies)
                }
                .onFailure { exception ->
                    _uiState.value = MoviesUiState.Error(exception.message ?: "Unknown error")
                }
        }
    }

    private fun loadGenres() {
        viewModelScope.launch {
            repository.getGenres()
                .onSuccess { genres ->
                    _genres.value = genres
                }
                .onFailure {
                    // Silently fail, genres are optional
                }
        }
    }
}

