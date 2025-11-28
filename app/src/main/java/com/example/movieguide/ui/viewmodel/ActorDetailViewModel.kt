package com.example.movieguide.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieguide.data.model.ActorDetail
import com.example.movieguide.data.model.Movie
import com.example.movieguide.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ActorDetailUiState {
    object Loading : ActorDetailUiState()
    data class Success(
        val actorDetail: ActorDetail,
        val movies: List<Movie>
    ) : ActorDetailUiState()
    data class Error(val message: String) : ActorDetailUiState()
}

class ActorDetailViewModel(
    application: Application,
    private val movieRepository: MovieRepository = MovieRepository()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<ActorDetailUiState>(ActorDetailUiState.Loading)
    val uiState: StateFlow<ActorDetailUiState> = _uiState.asStateFlow()

    fun loadActorDetails(actorId: Int) {
        viewModelScope.launch {
            _uiState.value = ActorDetailUiState.Loading
            
            val actorDetailResult = movieRepository.getActorDetails(actorId)
            val moviesResult = movieRepository.getActorMovieCredits(actorId)

            when {
                actorDetailResult.isFailure -> {
                    _uiState.value = ActorDetailUiState.Error(
                        actorDetailResult.exceptionOrNull()?.message ?: "Не удалось загрузить информацию об актёре"
                    )
                }
                else -> {
                    val actorDetail = actorDetailResult.getOrNull()
                    if (actorDetail != null) {
                        val movies = moviesResult.getOrElse { emptyList() }
                        
                        _uiState.value = ActorDetailUiState.Success(actorDetail, movies)
                    } else {
                        _uiState.value = ActorDetailUiState.Error("Не удалось загрузить информацию об актёре")
                    }
                }
            }
        }
    }
}

