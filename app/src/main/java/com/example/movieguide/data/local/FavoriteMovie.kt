package com.example.movieguide.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_movies")
data class FavoriteMovie(
    @PrimaryKey
    val id: Int,
    val title: String,
    val posterPath: String?,
    val voteAverage: Double?,
    val releaseDate: String?,
    val overview: String?,
    val backdropPath: String?,
    val runtime: Int?,
    val addedAt: Long = System.currentTimeMillis()
)

