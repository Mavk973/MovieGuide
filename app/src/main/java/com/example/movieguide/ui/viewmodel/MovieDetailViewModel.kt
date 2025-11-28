package com.example.movieguide.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieguide.data.local.AppDatabase
import com.example.movieguide.data.model.Cast
import com.example.movieguide.data.model.MovieDetail
import com.example.movieguide.data.model.Video
import com.example.movieguide.data.repository.FavoritesRepository
import com.example.movieguide.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class MovieDetailUiState {
    object Loading : MovieDetailUiState()
    data class Success(
        val movieDetail: MovieDetail,
        val cast: List<Cast>,
        val videos: List<Video>
    ) : MovieDetailUiState()
    data class Error(val message: String) : MovieDetailUiState()
}

class MovieDetailViewModel(
    application: Application,
    private val movieRepository: MovieRepository = MovieRepository()
) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val favoritesRepository = FavoritesRepository(database)

    private val _uiState = MutableStateFlow<MovieDetailUiState>(MovieDetailUiState.Loading)
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    fun loadMovieDetails(movieId: Int) {
        viewModelScope.launch {
            _uiState.value = MovieDetailUiState.Loading
            
            val movieDetailResult = movieRepository.getMovieDetails(movieId)
            val creditsResult = movieRepository.getMovieCredits(movieId)
            val videosResult = movieRepository.getMovieVideos(movieId)

            when {
                movieDetailResult.isFailure -> {
                    _uiState.value = MovieDetailUiState.Error(
                        movieDetailResult.exceptionOrNull()?.message ?: "Failed to load movie details"
                    )
                }
                else -> {
                    val movieDetail = movieDetailResult.getOrNull()
                    if (movieDetail != null) {
                        val cast = creditsResult.getOrElse { emptyList() }
                        val videos = videosResult.getOrElse { emptyList() }
                        
                        _uiState.value = MovieDetailUiState.Success(movieDetail, cast, videos)
                    } else {
                        _uiState.value = MovieDetailUiState.Error("Failed to load movie details")
                    }
                }
            }
        }
        
        // Observe favorite status
        favoritesRepository.isFavorite(movieId)
            .onEach { isFav ->
                _isFavorite.value = isFav
            }
            .launchIn(viewModelScope)
    }

    fun toggleFavorite(movieDetail: MovieDetail) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(movieDetail)
        }
    }
}

