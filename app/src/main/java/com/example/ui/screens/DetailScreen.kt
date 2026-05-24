package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.MediaItem
import com.example.viewmodel.CinevaultViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    mediaId: String,
    viewModel: CinevaultViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.allMediaItems.collectAsState()
    val settings by viewModel.settingsState.collectAsState()
    val item = remember(items, mediaId) { items.find { it.id == mediaId } }

    var episodesList by remember { mutableStateOf<List<com.example.data.jellyfin.JellyfinItem>>(emptyList()) }
    var selectedEpisode by remember { mutableStateOf<com.example.data.jellyfin.JellyfinItem?>(null) }
    var isLoadingEpisodes by remember { mutableStateOf(false) }
    var episodesError by remember { mutableStateOf<String?>(null) }
    var reloadTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(item, settings, reloadTrigger) {
        if (item != null && item.isSeries) {
            if (item.id.startsWith("jellyfin_") && settings.jellyfinServerUrl.isNotEmpty()) {
                isLoadingEpisodes = true
                episodesError = null
                try {
                    val itemId = item.id.removePrefix("jellyfin_")
                    val cleanUrl = if (settings.jellyfinServerUrl.endsWith("/")) settings.jellyfinServerUrl.dropLast(1) else settings.jellyfinServerUrl
                    val token = settings.jellyfinToken
                    val api = com.example.data.jellyfin.JellyfinClient.getApi(cleanUrl)
                    val authHeader = com.example.data.jellyfin.JellyfinClient.makeAuthHeader(token)
                    val response = api.getEpisodes(authHeader, itemId, settings.jellyfinUserId)
                    val sorted = response.items.sortedWith(
                        compareBy<com.example.data.jellyfin.JellyfinItem> { it.parentIndexNumber ?: 1 }
                            .thenBy { it.indexNumber ?: 1 }
                    )
                    episodesList = sorted
                    selectedEpisode = sorted.firstOrNull()
                } catch (e: Exception) {
                    android.util.Log.e("DetailScreen", "Error loading episodes", e)
                    episodesError = "Não foi possível carregar os episódios do servidor."
                } finally {
                    isLoadingEpisodes = false
                }
            } else {
                val mock = listOf(
                    com.example.data.jellyfin.JellyfinItem(
                        id = "mock_ep1",
                        name = "Choque no Sistema",
                        type = "Episode",
                        parentIndexNumber = 1,
                        indexNumber = 1,
                        overview = "O início de tudo. O jovem Virgil ganha poderes elétricos após ser exposto a um gás estranho nas docas.",
                        runTimeTicks = 12600000000L
                    ),
                    com.example.data.jellyfin.JellyfinItem(
                        id = "mock_ep2",
                        name = "Depois do Choque",
                        type = "Episode",
                        parentIndexNumber = 1,
                        indexNumber = 2,
                        overview = "Virgil aprende a controlar seus novos poderes enquanto lida com as dificuldades da escola.",
                        runTimeTicks = 13200000000L
                    ),
                    com.example.data.jellyfin.JellyfinItem(
                        id = "mock_ep3",
                        name = "Ameaça de Fogo",
                        type = "Episode",
                        parentIndexNumber = 1,
                        indexNumber = 3,
                        overview = "Um novo vilão com poderes pirocinéticos ameaça o bairro, forçando Virgil a agir de forma heroica.",
                        runTimeTicks = 12600000000L
                    ),
                    com.example.data.jellyfin.JellyfinItem(
                        id = "mock_ep4",
                        name = "A Aliança Cósmica",
                        type = "Episode",
                        parentIndexNumber = 1,
                        indexNumber = 4,
                        overview = "Parcerias inesperadas surgem quando super-heróis de outras partes aparecem na cidade de Dakota.",
                        runTimeTicks = 14400000000L
                    )
                )
                episodesList = mock
                selectedEpisode = mock.firstOrNull()
            }
        }
    }

    if (item == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Mídia não encontrada.")
        }
        return
    }

    val backdropUrl = remember(item, settings) {
        if (item.id.startsWith("jellyfin_") && settings.jellyfinServerUrl.isNotEmpty()) {
            val itemId = item.id.removePrefix("jellyfin_")
            val cleanUrl = if (settings.jellyfinServerUrl.endsWith("/")) settings.jellyfinServerUrl.dropLast(1) else settings.jellyfinServerUrl
            val tokenParam = if (settings.jellyfinToken.isNotEmpty()) "?api_key=${settings.jellyfinToken}" else ""
            "$cleanUrl/Items/$itemId/Images/Primary$tokenParam" // or Backdrop if needed
        } else {
            null
        }
    }

    val inWatchlist = item.inWatchlist
    val scrollState = rememberScrollState()

    // Similar items based on genre/writers/director matching
    val similarItems = remember(items, item) {
        items.filter { it.id != item.id && (it.genres.split(",").any { g -> item.genres.contains(g.trim()) } || it.director == item.director) }
            .take(5)
    }

    if (item.isSeries) {
        var selectedSeasonNum by remember { mutableStateOf<Int?>(null) }

        BackHandler(enabled = selectedSeasonNum != null) {
            selectedSeasonNum = null
        }

        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val isWidescreen = maxWidth > 600.dp && maxWidth > maxHeight

            if (isWidescreen) {
                // WIDESCREEN TABLET/PC LAYOUT (Side-by-Side Split)
                Row(modifier = Modifier.fillMaxSize()) {
                    // LEFT COLUMN (Pinned Poster - 260.dp to allow plenty of space)
                    Box(
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight()
                            .background(Color.Black)
                    ) {
                        if (backdropUrl != null) {
                            AsyncImage(
                                model = backdropUrl,
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
                                    text = item.title,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White.copy(alpha = 0.3f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        // Elegant Back Button Overlay
                        IconButton(
                            onClick = { viewModel.navigateTo(Screen.Home) },
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(16.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar",
                                tint = Color.White
                            )
                        }
                    }

                    // RIGHT COLUMN (Scrollable Details or Episode List)
                    val seasonsMap = remember(episodesList) {
                        episodesList.groupBy { it.parentIndexNumber ?: 1 }
                    }
                    val sortedSeasons = remember(seasonsMap) {
                        seasonsMap.keys.sorted().ifEmpty { listOf(1) }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .statusBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (selectedSeasonNum == null) {
                            // MAIN GENERAL VIEW (Image 1 / Image 3)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    // Title
                                    Text(
                                        text = item.title,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Metadata Subtitle
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.detailsSubtitle,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text("•", color = Color.White.copy(alpha = 0.4f))
                                        Text(
                                            text = "${item.year}",
                                            fontSize = 13.sp,
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                        Text("•", color = Color.White.copy(alpha = 0.4f))
                                        Box(
                                            modifier = Modifier
                                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = item.rating,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Text("•", color = Color.White.copy(alpha = 0.4f))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Rating",
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = item.rating.ifBlank { "7.7" },
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                // ACTIONS TOOLBAR
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.navigateTo(Screen.Player(item.id, selectedEpisode?.id)) },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.15f))
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                                    }
                                    IconButton(
                                        onClick = {
                                            val randomEp = episodesList.randomOrNull() ?: selectedEpisode
                                            viewModel.navigateTo(Screen.Player(item.id, randomEp?.id))
                                        },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.1f))
                                    ) {
                                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = Color.White)
                                    }
                                    IconButton(
                                        onClick = { /* Mark as watched */ },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.1f))
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Visto", tint = Color.White)
                                    }
                                    IconButton(
                                        onClick = { viewModel.toggleFavorite(item.id) },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.1f))
                                    ) {
                                        Icon(
                                            imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Favoritar",
                                            tint = if (item.isFavorite) Color.Red else Color.White
                                        )
                                    }
                                    IconButton(
                                        onClick = { /* Options */ },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.1f))
                                    ) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Opções", tint = Color.White)
                                    }
                                }
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

                            // SYNOPSIS
                            Text(
                                text = item.synopsis,
                                fontSize = 15.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 22.sp
                            )

                            // EXTRA SPECS BOX
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (item.tags.isNotEmpty()) {
                                    Row {
                                        Text(
                                            text = "Etiquetas: ",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.width(90.dp)
                                        )
                                        Text(
                                            text = item.tags,
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                                if (item.genres.isNotEmpty()) {
                                    Row {
                                        Text(
                                            text = "Gêneros: ",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.width(90.dp)
                                        )
                                        Text(
                                            text = item.genres,
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            // UP NEXT
                            if (selectedEpisode != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "A seguir",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )

                                Column(
                                    modifier = Modifier
                                        .width(280.dp)
                                        .clickable {
                                            viewModel.navigateTo(Screen.Player(item.id, selectedEpisode?.id))
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1.77f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1B1B22))
                                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(Color(0xFF282830), Color(0xFF101015))
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Tv,
                                                contentDescription = null,
                                                tint = Color.White.copy(alpha = 0.15f),
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.55f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "S1:E${selectedEpisode?.indexNumber ?: 1} - ${selectedEpisode?.name ?: item.title}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // SEASONS LIST
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Temporadas",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                sortedSeasons.forEach { seasonNum ->
                                    val epCount = seasonsMap[seasonNum]?.size ?: 5
                                    Column(
                                        modifier = Modifier
                                            .width(110.dp)
                                            .clickable { selectedSeasonNum = seasonNum },
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(110.dp)
                                                .height(160.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF16161C))
                                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                        ) {
                                            if (backdropUrl != null) {
                                                AsyncImage(
                                                    model = backdropUrl,
                                                    contentDescription = "Temporada $seasonNum",
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
                                                        text = "EPISÓDIOS",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color.White.copy(alpha = 0.3f)
                                                    )
                                                }
                                            }

                                            // Small badge carrying epCount
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
                                                    text = "$epCount",
                                                    color = Color.Black,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Temporada $seasonNum",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                        } else {
                            // EPISODES LIST (Image 2)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { selectedSeasonNum = null },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowBack,
                                                contentDescription = "Voltar",
                                                tint = Color(0xFF00D2FF)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = item.title,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = "Temporada $selectedSeasonNum",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF00D2FF),
                                        modifier = Modifier.padding(start = 40.dp)
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            val seasonEps = seasonsMap[selectedSeasonNum] ?: emptyList()
                                            val firstEp = seasonEps.firstOrNull() ?: selectedEpisode
                                            viewModel.navigateTo(Screen.Player(item.id, firstEp?.id))
                                        },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.15f))
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                                    }
                                    IconButton(
                                        onClick = {
                                            val seasonEps = seasonsMap[selectedSeasonNum] ?: emptyList()
                                            val randomEp = seasonEps.randomOrNull() ?: selectedEpisode
                                            viewModel.navigateTo(Screen.Player(item.id, randomEp?.id))
                                        },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.1f))
                                    ) {
                                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = Color.White)
                                    }
                                }
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))

                            val seasonEpisodes = seasonsMap[selectedSeasonNum] ?: emptyList()

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                seasonEpisodes.forEach { ep ->
                                    val isCurrentPlaying = ep.id == selectedEpisode?.id
                                    val epImageUrl = remember(ep, settings) {
                                        if (ep.id.startsWith("mock_")) {
                                            null
                                        } else {
                                            val cleanUrl = if (settings.jellyfinServerUrl.endsWith("/")) settings.jellyfinServerUrl.dropLast(1) else settings.jellyfinServerUrl
                                            val tokenParam = if (settings.jellyfinToken.isNotEmpty()) "?api_key=${settings.jellyfinToken}" else ""
                                            "$cleanUrl/Items/${ep.id}/Images/Primary$tokenParam"
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isCurrentPlaying) Color.White.copy(alpha = 0.06f) else Color.Transparent)
                                            .clickable {
                                                selectedEpisode = ep
                                                viewModel.navigateTo(Screen.Player(item.id, ep.id))
                                            }
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(140.dp)
                                                .aspectRatio(1.77f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF1B1B22))
                                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                        ) {
                                            if (epImageUrl != null) {
                                                AsyncImage(
                                                    model = epImageUrl,
                                                    contentDescription = ep.name,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            Brush.verticalGradient(
                                                                colors = listOf(Color(0xFF282830), Color(0xFF101015))
                                                            )
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Tv,
                                                        contentDescription = null,
                                                        tint = Color.White.copy(alpha = 0.15f),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Black.copy(alpha = 0.5f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${ep.indexNumber ?: 1}. ${ep.name}",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrentPlaying) Color(0xFF00D2FF) else Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))

                                            val durationStr = remember(ep) {
                                                val ticks = ep.runTimeTicks ?: 0L
                                                val m = ticks / 600000000L
                                                val h = m / 60
                                                val mins = m % 60
                                                if (h > 0) "${h}h ${mins}m" else "${m}m"
                                            }

                                            val terminalTimeStr = remember(ep) {
                                                val ticks = ep.runTimeTicks ?: 0L
                                                val minutes = (ticks / 600000000L).toInt().coerceAtLeast(21)
                                                val calendar = java.util.Calendar.getInstance()
                                                calendar.add(java.util.Calendar.MINUTE, minutes)
                                                val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                                "Termina às ${sdf.format(calendar.time)}"
                                            }

                                            Text(
                                                text = "$durationStr  •  $terminalTimeStr",
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Informações",
                                                tint = Color.White.copy(alpha = 0.4f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Confirmar",
                                                tint = Color.White.copy(alpha = 0.4f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Icon(
                                                imageVector = Icons.Default.FavoriteBorder,
                                                contentDescription = "Adicionar a favorito",
                                                tint = Color.White.copy(alpha = 0.4f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Mais opções",
                                                tint = Color.White.copy(alpha = 0.4f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // MOBILE/COMPACT LAYOUT - BEAUTIFUL & RESPONSIVE VERTICAL ALIGNMENT (Fits standard smartphones!)
                val seasonsMap = remember(episodesList) {
                    episodesList.groupBy { it.parentIndexNumber ?: 1 }
                }
                val sortedSeasons = remember(seasonsMap) {
                    seasonsMap.keys.sorted().ifEmpty { listOf(1) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // HERO BACKDROP BANNER FOR SERIES
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .background(Color.Black)
                    ) {
                        if (backdropUrl != null) {
                            AsyncImage(
                                model = backdropUrl,
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
                                    text = item.title,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White.copy(alpha = 0.3f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        // Bottom gradient fading to main background color
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.4f),
                                            MaterialTheme.colorScheme.background
                                        )
                                    )
                                )
                        )

                        // Central Play Indicator
                        IconButton(
                            onClick = { viewModel.navigateTo(Screen.Player(item.id, selectedEpisode?.id)) },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Reproduzir",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                              )
                        }

                        // Elegant Navigation Back Button Overlay (Red Circle with white arrow to return Home)
                        IconButton(
                            onClick = {
                                viewModel.setActiveHomeMode("home")
                                viewModel.navigateTo(Screen.Home)
                            },
                            modifier = Modifier
                                .statusBarsPadding()
                                .padding(12.dp)
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE50914))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar ao Início",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // MAIN PANEL below hero banner
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (selectedSeasonNum == null) {
                            // MOBILE MAIN VIEW
                            Text(
                                text = item.title,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )

                            // Metadata Subtitle
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.detailsSubtitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("•", color = Color.White.copy(alpha = 0.4f))
                                Text(
                                    text = "${item.year}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Text("•", color = Color.White.copy(alpha = 0.4f))
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.rating,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // ACTION OVERLAY TOOLBAR (Responsive mobile-friendly buttons)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.navigateTo(Screen.Player(item.id, selectedEpisode?.id)) },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black, modifier = Modifier.size(22.dp))
                                }
                                IconButton(
                                    onClick = {
                                        val randomEp = episodesList.randomOrNull() ?: selectedEpisode
                                        viewModel.navigateTo(Screen.Player(item.id, randomEp?.id))
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.12f))
                                ) {
                                    Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                IconButton(
                                    onClick = { /* Mark as watched */ },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.12f))
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Visto", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                IconButton(
                                    onClick = { viewModel.toggleFavorite(item.id) },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.12f))
                                ) {
                                    Icon(
                                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favoritar",
                                        tint = if (item.isFavorite) Color.Red else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { /* Options */ },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.12f))
                                ) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Opções", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 2.dp))

                            // SYNOPSIS
                            Text(
                                text = item.synopsis,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 20.sp
                            )

                            // INFO SPECS (Genres & tags)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (item.tags.isNotEmpty()) {
                                    Row {
                                        Text("Etiquetas: ", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
                                        Text(item.tags, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                                    }
                                }
                                if (item.genres.isNotEmpty()) {
                                    Row {
                                        Text("Gêneros: ", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
                                        Text(item.genres, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                                    }
                                }
                            }

                            // UP NEXT
                            if (selectedEpisode != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("A seguir", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.navigateTo(Screen.Player(item.id, selectedEpisode?.id)) }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1.77f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1B1B22))
                                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(Color(0xFF282830), Color(0xFF101015))
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Tv, contentDescription = null, tint = Color.White.copy(alpha = 0.15f), modifier = Modifier.size(32.dp))
                                        }
                                        Box(
                                            modifier = Modifier.align(Alignment.Center).size(36.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.55f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("S1:E${selectedEpisode?.indexNumber ?: 1} - ${selectedEpisode?.name ?: item.title}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }

                            // SEASONS CARDS
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Temporadas", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                            
                            if (isLoadingEpisodes) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color(0xFF00D2FF))
                                }
                            } else if (episodesError != null) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = episodesError ?: "Erro ao carregar episódios",
                                        color = Color.Red.copy(alpha = 0.8f),
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = { reloadTrigger++ },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D2FF)),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Tentar Novamente", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    sortedSeasons.forEach { seasonNum ->
                                        val epCount = seasonsMap[seasonNum]?.size ?: 0
                                        Column(
                                            modifier = Modifier.width(90.dp).clickable { selectedSeasonNum = seasonNum }
                                        ) {
                                            Box(
                                                modifier = Modifier.width(90.dp).height(130.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFF16161C)).border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                            ) {
                                                if (backdropUrl != null) {
                                                    AsyncImage(model = backdropUrl, contentDescription = "Temporada $seasonNum", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                                }
                                                Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(18.dp).clip(CircleShape).background(Color(0xFF00D2FF)), contentAlignment = Alignment.Center) {
                                                    Text("$epCount", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Temporada $seasonNum", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        } else {
                            // MOBILE EPISODES LIST VIEW
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    IconButton(onClick = { selectedSeasonNum = null }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color(0xFF00D2FF))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text(item.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("Temporada $selectedSeasonNum", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF00D2FF))
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = {
                                            val seasonEps = seasonsMap[selectedSeasonNum] ?: emptyList()
                                            viewModel.navigateTo(Screen.Player(item.id, seasonEps.firstOrNull()?.id))
                                        },
                                        modifier = Modifier.size(34.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f))
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            val seasonEps = seasonsMap[selectedSeasonNum] ?: emptyList()
                                            viewModel.navigateTo(Screen.Player(item.id, seasonEps.randomOrNull()?.id))
                                        },
                                        modifier = Modifier.size(34.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f))
                                    ) {
                                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 2.dp))

                            val seasonEpisodes = seasonsMap[selectedSeasonNum] ?: emptyList()

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                seasonEpisodes.forEach { ep ->
                                    val isCurrentPlaying = ep.id == selectedEpisode?.id
                                    val epImageUrl = remember(ep, settings) {
                                        if (ep.id.startsWith("mock_")) {
                                            null
                                        } else {
                                            val cleanUrl = if (settings.jellyfinServerUrl.endsWith("/")) settings.jellyfinServerUrl.dropLast(1) else settings.jellyfinServerUrl
                                            val tokenParam = if (settings.jellyfinToken.isNotEmpty()) "?api_key=${settings.jellyfinToken}" else ""
                                            "$cleanUrl/Items/${ep.id}/Images/Primary$tokenParam"
                                        }
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isCurrentPlaying) Color.White.copy(alpha = 0.06f) else Color.Transparent)
                                            .clickable {
                                                selectedEpisode = ep
                                                viewModel.navigateTo(Screen.Player(item.id, ep.id))
                                            }
                                            .padding(6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.width(100.dp).aspectRatio(1.77f).clip(RoundedCornerShape(4.dp)).background(Color(0xFF1B1B22)).border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                        ) {
                                            if (epImageUrl != null) {
                                                AsyncImage(
                                                    model = epImageUrl,
                                                    contentDescription = ep.name,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color(0xFF282830), Color(0xFF101015)))), contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Tv, contentDescription = null, tint = Color.White.copy(alpha = 0.15f), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Box(modifier = Modifier.align(Alignment.Center).size(20.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${ep.indexNumber ?: 1}. ${ep.name}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrentPlaying) Color(0xFF00D2FF) else Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            val ticks = ep.runTimeTicks ?: 0L
                                            val m = ticks / 600000000L
                                            Text("${m}m", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                                        }

                                        // Info Icon
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "info",
                                            tint = Color.White.copy(alpha = 0.3f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .background(MaterialTheme.colorScheme.background)
            ) {
            // STANDARD FILM HERO BACKDROP BOX
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF35343A),
                                Color(0xFF0E0E13)
                            )
                        )
                    )
            ) {
                if (backdropUrl != null) {
                    AsyncImage(
                        model = backdropUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Overlay lighting mask matching Lumina Cinematic Design
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF131318).copy(alpha = 0.5f),
                                    Color(0xFF131318)
                                )
                            )
                        )
                )

                // Dynamic header contents
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Badges row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = item.tags.split(",").firstOrNull()?.trim() ?: "4K HDR",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = item.rating,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${item.year} • ${item.runtime}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = item.title,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        lineHeight = 38.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.navigateTo(Screen.Player(item.id)) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(50.dp)
                                .testTag("watch_now_button"),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Assistir Agora", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.toggleWatchlist(item.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (inWatchlist) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("watchlist_toggle_button")
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(25.dp)),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Icon(
                                imageVector = if (inWatchlist) Icons.Default.Check else Icons.Default.Add,
                                contentDescription = "My list"
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (inWatchlist) "Na Lista" else "Minha Lista", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Details content
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                // Synopsis section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Synopsis",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Sinopse",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = item.synopsis,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )
                }



                // "Títulos Semelhantes" related list
                if (similarItems.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Títulos Semelhantes",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Recomendado com base neste filme",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            similarItems.forEach { similar ->
                                HomePosterCard(
                                    item = similar,
                                    jellyfinServerUrl = settings.jellyfinServerUrl,
                                    jellyfinToken = settings.jellyfinToken,
                                    onClick = { viewModel.navigateTo(Screen.Detail(similar.id)) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Red Back circular button and Favorite overlayRow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.setActiveHomeMode("home")
                        viewModel.navigateTo(Screen.Home)
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE50914))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar ao Início",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleFavorite(item.id) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favoritar",
                        tint = if (item.isFavorite) Color.Red else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}

data class CastMember(
    val name: String,
    val character: String,
    val color: Color
)
