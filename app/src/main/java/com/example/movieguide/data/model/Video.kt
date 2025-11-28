package com.example.movieguide.data.model

import com.google.gson.annotations.SerializedName

data class Video(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String
)

data class VideosResponse(
    val results: List<Video>
)

