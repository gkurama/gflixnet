package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MediaItem
import com.example.viewmodel.CinevaultViewModel
import com.example.viewmodel.Screen
import coil.compose.AsyncImage

@Composable
fun HomeScreen(
    viewModel: CinevaultViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.allMediaItems.collectAsState()
    val settings by viewModel.settingsState.collectAsState()
    val activeHomeMode by viewModel.activeHomeMode.collectAsState()

    var homeTab by remember { mutableStateOf("Início") } // "Início" or "Minha Lista"
    val scrollState = rememberScrollState()

    // Secondary Horizontal Filter Tags
    val filterTags = remember(activeHomeMode) {
        when (activeHomeMode) {
            "filmes" -> listOf("Filmes", "Minha Lista", "Sugestões", "Trailers", "Coleções")
            "series" -> listOf("Séries", "Minha Lista", "Sugestões", "Próximos", "Géneros")
            "filmes_biblicos" -> listOf("Bíblicos", "Minha Lista", "Sugestões", "Trailers")
            "series_biblicas" -> listOf("Bíblicas", "Minha Lista", "Sugestões", "Próximos")
            else -> listOf("Músicas", "Álbuns", "Artistas", "Playlists")
        }
    }
    var activeFilterTag by remember(filterTags) { mutableStateOf(filterTags.firstOrNull() ?: "") }

    // Filter items based on activeHomeMode and activeFilterTag
    val displayItems = remember(items, activeHomeMode, activeFilterTag) {
        val baseList = when (activeHomeMode) {
            "filmes" -> items.filter { 
                !it.isSeries && 
                it.detailsSubtitle != "Música" && 
                !it.libraryName.contains("Bibl", ignoreCase = true) && 
                !it.genres.contains("Bíblico", ignoreCase = true) &&
                !it.genres.contains("Bíblica", ignoreCase = true)
            }
            "series" -> items.filter { 
                it.isSeries && 
                !it.libraryName.contains("Bibl", ignoreCase = true) && 
                !it.genres.contains("Bíblico", ignoreCase = true) &&
                !it.genres.contains("Bíblica", ignoreCase = true)
            }
            "filmes_biblicos" -> items.filter { 
                !it.isSeries && 
                (it.libraryName.contains("Bibl", ignoreCase = true) ||
                 it.genres.contains("Bíblico", ignoreCase = true) ||
                 it.genres.contains("Bíblica", ignoreCase = true) ||
                 it.genres.contains("Religioso", ignoreCase = true) ||
                 it.title.contains("Bíblico", ignoreCase = true) ||
                 it.title.contains("Bíblica", ignoreCase = true))
            }
            "series_biblicas" -> items.filter { 
                it.isSeries && 
                (it.libraryName.contains("Bibl", ignoreCase = true) ||
                 it.genres.contains("Bíblico", ignoreCase = true) ||
                 it.genres.contains("Bíblica", ignoreCase = true) ||
                 it.genres.contains("Religioso", ignoreCase = true) ||
                 it.title.contains("Bíblico", ignoreCase = true) ||
                 it.title.contains("Bíblica", ignoreCase = true))
            }
            "musica" -> items.filter { 
                it.detailsSubtitle == "Música"
            }
            else -> items
        }

        if (activeFilterTag == "Minha Lista" || activeFilterTag == "Favoritos") {
            baseList.filter { it.inWatchlist || it.isFavorite }
        } else {
            baseList
        }
    }

    val continueWatching = remember(items) {
        items.filter { it.userProgressPercent > 0 }
            .sortedByDescending { it.lastWatchedTimestamp }
    }

    val recentMovies = remember(items) {
        items.filter { 
            !it.isSeries && 
            it.detailsSubtitle != "Música" &&
            !it.libraryName.contains("Bibl", ignoreCase = true) &&
            !it.genres.contains("Bíblico", ignoreCase = true) &&
            !it.genres.contains("Bíblica", ignoreCase = true)
        }.take(10)
    }

    val recentSeries = remember(items) {
        items.filter { 
            it.isSeries &&
            !it.libraryName.contains("Bibl", ignoreCase = true) &&
            !it.genres.contains("Bíblico", ignoreCase = true) &&
            !it.genres.contains("Bíblica", ignoreCase = true)
        }.take(10)
    }

    val recentBiblicalMovies = remember(items) {
        items.filter { 
            !it.isSeries && 
            (it.libraryName.contains("Bibl", ignoreCase = true) ||
             it.genres.contains("Bíblico", ignoreCase = true) ||
             it.genres.contains("Bíblica", ignoreCase = true) ||
             it.genres.contains("Religioso", ignoreCase = true) ||
             it.title.contains("Bíblico", ignoreCase = true) ||
             it.title.contains("Bíblica", ignoreCase = true))
        }.take(10)
    }

    val recentBiblicalSeries = remember(items) {
        items.filter { 
            it.isSeries && 
            (it.libraryName.contains("Bibl", ignoreCase = true) ||
             it.genres.contains("Bíblico", ignoreCase = true) ||
             it.genres.contains("Bíblica", ignoreCase = true) ||
             it.genres.contains("Religioso", ignoreCase = true) ||
             it.title.contains("Bíblico", ignoreCase = true) ||
             it.title.contains("Bíblica", ignoreCase = true))
        }.take(10)
    }

    val recentMusic = remember(items) {
        items.filter { it.detailsSubtitle == "Música" }.take(10)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (activeHomeMode == "home") {
            // Home tab select (Início | Minha Lista) - Underlined Style (Image 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Início", "Minha Lista").forEach { tab ->
                    val isSelected = tab == homeTab
                    Column(
                        modifier = Modifier
                            .clickable { homeTab = tab }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = tab,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF00D2FF) else Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(2.dp)
                                    .background(Color(0xFF00D2FF))
                            )
                        } else {
                            Box(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }

            if (homeTab == "Início") {
                // Section: O meu conteúdo
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "O meu conteúdo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Categories Cards (Horizontally Scrollable)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryCard(
                            title = "FILMES",
                            subtitle = "Filmes",
                            icon = Icons.Default.Movie,
                            onClick = { viewModel.setActiveHomeMode("filmes") },
                            modifier = Modifier.width(106.dp)
                        )
                        CategoryCard(
                            title = "SÉRIES",
                            subtitle = "Séries",
                            icon = Icons.Default.Tv,
                            onClick = { viewModel.setActiveHomeMode("series") },
                            modifier = Modifier.width(106.dp)
                        )
                        CategoryCard(
                            title = "FILMES BÍBLICOS",
                            subtitle = "Filmes Bíblicos",
                            icon = Icons.Default.MenuBook,
                            onClick = { viewModel.setActiveHomeMode("filmes_biblicos") },
                            modifier = Modifier.width(124.dp)
                        )
                        CategoryCard(
                            title = "SÉRIES BÍBLICAS",
                            subtitle = "Séries Bíblicas",
                            icon = Icons.Default.LibraryBooks,
                            onClick = { viewModel.setActiveHomeMode("series_biblicas") },
                            modifier = Modifier.width(124.dp)
                        )
                        CategoryCard(
                            title = "MÚSICA",
                            subtitle = "Música",
                            icon = Icons.Default.MusicNote,
                            onClick = { viewModel.setActiveHomeMode("musica") },
                            modifier = Modifier.width(106.dp)
                        )
                    }
                }

                // Section: Continuar a Ver (A seguir)
                if (continueWatching.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "A seguir >",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            continueWatching.forEach { item ->
                                ContinueWatchingCard(
                                    item = item,
                                    onClick = { viewModel.navigateTo(Screen.Detail(item.id)) },
                                    onPlay = { viewModel.navigateTo(Screen.Player(item.id)) }
                                )
                            }
                        }
                    }
                }

                // Section: Filmes recentes >
                if (recentMovies.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setActiveHomeMode("filmes") },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filmes recentes >",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            recentMovies.forEach { item ->
                                HomePosterCard(
                                    item = item,
                                    jellyfinServerUrl = settings.jellyfinServerUrl,
                                    jellyfinToken = settings.jellyfinToken,
                                    onClick = { viewModel.navigateTo(Screen.Detail(item.id)) }
                                )
                            }
                        }
                    }
                }

                // Section: Séries recentes >
                if (recentSeries.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setActiveHomeMode("series") },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Séries recentes >",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            recentSeries.forEach { item ->
                                HomePosterCard(
                                    item = item,
                                    jellyfinServerUrl = settings.jellyfinServerUrl,
                                    jellyfinToken = settings.jellyfinToken,
                                    onClick = { viewModel.navigateTo(Screen.Detail(item.id)) }
                                )
                            }
                        }
                    }
                }

                // Section: Filmes Bíblicos recentes >
                if (recentBiblicalMovies.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setActiveHomeMode("filmes_biblicos") },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filmes Bílicos recentes >",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            recentBiblicalMovies.forEach { item ->
                                HomePosterCard(
                                    item = item,
                                    jellyfinServerUrl = settings.jellyfinServerUrl,
                                    jellyfinToken = settings.jellyfinToken,
                                    onClick = { viewModel.navigateTo(Screen.Detail(item.id)) }
                                )
                            }
                        }
                    }
                }

                // Section: Séries Bíblicas recentes >
                if (recentBiblicalSeries.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setActiveHomeMode("series_biblicas") },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Séries Bíblicas recentes >",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            recentBiblicalSeries.forEach { item ->
                                HomePosterCard(
                                    item = item,
                                    jellyfinServerUrl = settings.jellyfinServerUrl,
                                    jellyfinToken = settings.jellyfinToken,
                                    onClick = { viewModel.navigateTo(Screen.Detail(item.id)) }
                                )
                            }
                        }
                    }
                }

                // Section: Músicas recentes >
                if (recentMusic.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setActiveHomeMode("musica") },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Músicas recentes >",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            recentMusic.forEach { item ->
                                HomePosterCard(
                                    item = item,
                                    jellyfinServerUrl = settings.jellyfinServerUrl,
                                    jellyfinToken = settings.jellyfinToken,
                                    onClick = { viewModel.navigateTo(Screen.Detail(item.id)) }
                                )
                            }
                        }
                    }
                }
            } else {
                // Minha Lista Tab List (Items added to watchlist or favorited)
                val favorites = items.filter { it.inWatchlist || it.isFavorite }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Minha Lista",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (favorites.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Você ainda não adicionou nenhuma mídia à sua lista.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        // Display Favorite Grid (3 Columns)
                        val chunkedFavs = favorites.chunked(3)
                        chunkedFavs.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowItems.forEach { item ->
                                    GridPosterCard(
                                        item = item,
                                        jellyfinServerUrl = settings.jellyfinServerUrl,
                                        jellyfinToken = settings.jellyfinToken,
                                        onClick = { viewModel.navigateTo(Screen.Detail(item.id)) },
                                        isSeriesMode = item.isSeries,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                val emptySlots = 3 - rowItems.size
                                if (emptySlots > 0) {
                                    repeat(emptySlots) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // LIST MODES Layout ("filmes", "series", "musica") - Matches Image 2 and 3 exactly!
            
            // Back Button Row to go back to Home (Match the red circle with white arrow)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clickable { viewModel.setActiveHomeMode("home") },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE50914)), // Netflix/Gflixnet Red
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar ao Início",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Voltar ao Início",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Toque para retornar à página principal",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                filterTags.forEach { tag ->
                    val isTagSelected = tag == activeFilterTag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isTagSelected) Color(0xFF00D2FF).copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { activeFilterTag = tag }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tag,
                            color = if (isTagSelected) Color(0xFF00D2FF) else Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Shuffle, Grid, Alpha-Order, Filters toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1-${displayItems.size} de ${displayItems.size}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Embaralhar",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp).clickable {}
                    )
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Visualização",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp).clickable {}
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {}
                    ) {
                        Text(
                            text = "A-Z",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Ordem",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtrar",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp).clickable {}
                    )
                }
            }

            // Grid Layout of Items alongside Alphabetical scroll strip (Image 2 and 3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Items Grid
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (displayItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nenhum item conectado do Jellyfin.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        val chunkedItems = displayItems.chunked(3)
                        chunkedItems.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowItems.forEach { item ->
                                    GridPosterCard(
                                        item = item,
                                        jellyfinServerUrl = settings.jellyfinServerUrl,
                                        jellyfinToken = settings.jellyfinToken,
                                        onClick = { viewModel.navigateTo(Screen.Detail(item.id)) },
                                        isSeriesMode = activeHomeMode == "series",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                val emptySlots = 3 - rowItems.size
                                if (emptySlots > 0) {
                                    repeat(emptySlots) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // Vertical Alphabetical Sidebar
                AlphabetSidebar(onLetterClick = {})
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun CategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.15f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF16161C))
                .border(2.dp, Color(0xFF00D2FF).copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF00D2FF),
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00D2FF).copy(alpha = 0.85f),
                    letterSpacing = 1.sp
                )
            }
        }
        Text(
            text = subtitle,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.75f)
        )
    }
}

@Composable
fun ContinueWatchingCard(
    item: MediaItem,
    onClick: () -> Unit,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16161C)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color(0xFF101014))
            ) {
                // Interactive play center visualizer
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { onPlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Assistir de onde parou",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Standard tag badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.tags.split(",").firstOrNull()?.trim() ?: "Jellyfin",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Progress text and bar
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item.userRemainingMinutes}m restantes",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${item.userProgressPercent}%",
                        fontSize = 10.sp,
                        color = Color(0xFF00D2FF),
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { item.userProgressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp)),
                    color = Color(0xFF00D2FF),
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
fun HomePosterCard(
    item: MediaItem,
    jellyfinServerUrl: String?,
    jellyfinToken: String?,
    onClick: () -> Unit
) {
    val imageUrl = if (item.id.startsWith("jellyfin_") && !jellyfinServerUrl.isNullOrBlank()) {
        val itemId = item.id.removePrefix("jellyfin_")
        val cleanUrl = if (jellyfinServerUrl.endsWith("/")) jellyfinServerUrl.dropLast(1) else jellyfinServerUrl
        val tokenParam = if (!jellyfinToken.isNullOrBlank()) "&api_key=$jellyfinToken" else ""
        "$cleanUrl/Items/$itemId/Images/Primary?maxWidth=180$tokenParam"
    } else {
        null
    }

    Column(
        modifier = Modifier
            .width(110.dp)
            .clickable { onClick() },
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(160.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF16161C))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF2E2E38), Color(0xFF14141A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.title.firstOrNull()?.toString() ?: "M",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00D2FF).copy(alpha = 0.2f)
                    )
                }
            }
        }

        Text(
            text = item.title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun GridPosterCard(
    item: MediaItem,
    jellyfinServerUrl: String?,
    jellyfinToken: String?,
    onClick: () -> Unit,
    isSeriesMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val imageUrl = if (item.id.startsWith("jellyfin_") && !jellyfinServerUrl.isNullOrBlank()) {
        val itemId = item.id.removePrefix("jellyfin_")
        val cleanUrl = if (jellyfinServerUrl.endsWith("/")) jellyfinServerUrl.dropLast(1) else jellyfinServerUrl
        val tokenParam = if (!jellyfinToken.isNullOrBlank()) "&api_key=$jellyfinToken" else ""
        "$cleanUrl/Items/$itemId/Images/Primary?maxWidth=240$tokenParam"
    } else {
        null
    }

    // Floating badges for episodes/remaining counts
    val seriesBadgeText = remember(item.id) {
        if (isSeriesMode) {
            listOf("4", "8", "12", "15", "16", "24", "41", "99+").random()
        } else {
            null
        }
    }

    Column(
        modifier = modifier
            .clickable { onClick() }
            .testTag("grid_item_${item.id}"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1C1C22))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF2E2E38), Color(0xFF14141A))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.title.firstOrNull()?.toString() ?: "M",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00D2FF).copy(alpha = 0.2f)
                    )
                }
            }

            // Floated Notification Badge (Image 3)
            if (isSeriesMode && seriesBadgeText != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00D2FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = seriesBadgeText,
                        color = Color.Black,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Circular Play Overlay (Image 1)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Reproduzir",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Text(
            text = item.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = item.year.toString(),
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.5f),
            maxLines = 1,
            fontWeight = FontWeight.Medium,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AlphabetSidebar(
    onLetterClick: (Char) -> Unit
) {
    val letters = listOf('#') + ('A'..'Z').toList()
    Column(
        modifier = Modifier
            .width(18.dp)
            .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        letters.forEach { char ->
            Text(
                text = char.toString(),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier
                    .clickable { onLetterClick(char) }
                    .padding(vertical = 1.dp)
            )
        }
    }
}
