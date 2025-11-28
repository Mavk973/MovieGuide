package com.example.movieguide.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movieguide.data.api.RetrofitClient
import com.example.movieguide.data.model.Cast
import com.example.movieguide.data.model.MovieDetail
import com.example.movieguide.data.model.Video
import com.example.movieguide.ui.viewmodel.MovieDetailUiState
import com.example.movieguide.ui.viewmodel.MovieDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Int,
    onBackClick: () -> Unit,
    viewModel: MovieDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(movieId) {
        viewModel.loadMovieDetails(movieId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали фильма") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is MovieDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is MovieDetailUiState.Success -> {
                    MovieDetailContent(
                        movieDetail = state.movieDetail,
                        cast = state.cast,
                        videos = state.videos
                    )
                }
                is MovieDetailUiState.Error -> {
                    ErrorMessage(
                        message = state.message,
                        onRetry = { viewModel.loadMovieDetails(movieId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun MovieDetailContent(
    movieDetail: MovieDetail,
    cast: List<Cast>,
    videos: List<Video>
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Backdrop image
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(RetrofitClient.getBackdropUrl(movieDetail.backdropPath))
                .crossfade(true)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .build(),
            contentDescription = movieDetail.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and rating
            Text(
                text = movieDetail.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "⭐ ${String.format("%.1f", movieDetail.voteAverage)}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = movieDetail.releaseDate.take(4),
                    style = MaterialTheme.typography.titleMedium
                )
                if (movieDetail.runtime != null) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${movieDetail.runtime} мин",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Genres
            if (movieDetail.genres.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = movieDetail.genres.joinToString(", ") { it.name },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Overview
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Описание",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = movieDetail.overview.ifEmpty { "Описание отсутствует" },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Justify
            )

            // Trailer button
            val trailerVideo = videos.firstOrNull { it.site == "YouTube" && it.type == "Trailer" }
            if (trailerVideo != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.youtube.com/watch?v=${trailerVideo.key}")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Смотреть трейлер")
                }
            }

            // Cast section
            if (cast.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "В главных ролях",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cast.take(10)) { castMember ->
                        CastItem(cast = castMember)
                    }
                }
            }
        }
    }
}

@Composable
fun CastItem(cast: Cast) {
    Column(
        modifier = Modifier.width(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(RetrofitClient.getProfileUrl(cast.profilePath))
                .crossfade(true)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .build(),
            contentDescription = cast.name,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = cast.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            textAlign = TextAlign.Center
        )
        if (cast.character.isNotEmpty()) {
            Text(
                text = cast.character,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
        }
    }
}

