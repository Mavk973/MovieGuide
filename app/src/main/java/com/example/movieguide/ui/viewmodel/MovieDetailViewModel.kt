package com.example.movieguide.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieguide.data.model.Cast
import com.example.movieguide.data.model.MovieDetail
import com.example.movieguide.data.model.Video
import com.example.movieguide.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val repository: MovieRepository = MovieRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<MovieDetailUiState>(MovieDetailUiState.Loading)
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    fun loadMovieDetails(movieId: Int) {
        viewModelScope.launch {
            _uiState.value = MovieDetailUiState.Loading
            
            val movieDetailResult = repository.getMovieDetails(movieId)
            val creditsResult = repository.getMovieCredits(movieId)
            val videosResult = repository.getMovieVideos(movieId)

            when {
                movieDetailResult.isFailure -> {
                    _uiState.value = MovieDetailUiState.Error(
                        movieDetailResult.exceptionOrNull()?.message ?: "Failed to load movie details"
                    )
                }
                else -> {
                    val movieDetail = movieDetailResult.getOrNull()!!
                    val cast = creditsResult.getOrElse { emptyList() }
                    val videos = videosResult.getOrElse { emptyList() }
                    
                    _uiState.value = MovieDetailUiState.Success(movieDetail, cast, videos)
                }
            }
        }
    }
}

