package com.example.movieguide.data.model

import com.google.gson.annotations.SerializedName

data class Movie(
    val id: Int,
    val title: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("vote_average")
    val voteAverage: Double? = null,
    @SerializedName("release_date")
    val releaseDate: String? = null
)

data class MovieResponse(
    val results: List<Movie>
)

