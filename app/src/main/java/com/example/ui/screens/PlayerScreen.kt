package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.common.Player
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.model.MediaItem
import com.example.viewmodel.CinevaultViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.layout.BoxWithConstraints

@Composable
fun PlayerScreen(
    mediaId: String,
    episodeId: String? = null,
    viewModel: CinevaultViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.allMediaItems.collectAsState()
    val settings by viewModel.settingsState.collectAsState()
    val item = remember(items, mediaId) { items.find { id -> id.id == mediaId } }

    if (item == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Mídia indisponível")
        }
        return
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activity = remember(context) {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is android.app.Activity) break
            ctx = ctx.baseContext
        }
        ctx as? android.app.Activity
    }

    val isInPipMode by viewModel.isInPipMode.collectAsState()

    val isAudio = remember(item) {
        item.detailsSubtitle == "Música"
    }

    val musicPlaylist = remember(items) {
        items.filter { it.detailsSubtitle == "Música" }
    }

    var seriesEpisodes by remember { mutableStateOf<List<com.example.data.jellyfin.JellyfinItem>>(emptyList()) }

    val previewUrl = remember(item, settings, episodeId, seriesEpisodes) {
        if (item.id.startsWith("jellyfin_") && settings.jellyfinServerUrl.isNotEmpty()) {
            val cleanUrl = if (settings.jellyfinServerUrl.endsWith("/")) settings.jellyfinServerUrl.dropLast(1) else settings.jellyfinServerUrl
            val tokenParam = if (settings.jellyfinToken.isNotEmpty()) "?api_key=${settings.jellyfinToken}" else ""
            if (item.isSeries) {
                val currentEpId = episodeId ?: seriesEpisodes.firstOrNull()?.id
                if (currentEpId != null) {
                    "$cleanUrl/Items/$currentEpId/Images/Primary$tokenParam"
                } else {
                    val itemId = item.id.removePrefix("jellyfin_")
                    "$cleanUrl/Items/$itemId/Images/Primary$tokenParam"
                }
            } else {
                val itemId = item.id.removePrefix("jellyfin_")
                "$cleanUrl/Items/$itemId/Images/Primary$tokenParam"
            }
        } else {
            when (item.id) {
                "dark_knight" -> "https://images.unsplash.com/photo-1547347295-31895e688a5d?w=400"
                "neon_nexus" -> "https://images.unsplash.com/photo-1578894381163-e72c17f2d45f?w=400"
                "dune_arrakis" -> "https://images.unsplash.com/photo-1547234935-80c7145ec969?w=400"
                else -> {
                    if (item.isSeries) {
                        "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=400"
                    } else {
                        "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=400"
                    }
                }
            }
        }
    }

    LaunchedEffect(item, settings) {
        if (item.isSeries) {
            if (item.id.startsWith("jellyfin_") && settings.jellyfinServerUrl.isNotEmpty()) {
                try {
                    val itemId = item.id.removePrefix("jellyfin_")
                    val cleanUrl = if (settings.jellyfinServerUrl.endsWith("/")) settings.jellyfinServerUrl.dropLast(1) else settings.jellyfinServerUrl
                    val token = settings.jellyfinToken
                    val api = com.example.data.jellyfin.JellyfinClient.getApi(cleanUrl)
                    val authHeader = com.example.data.jellyfin.JellyfinClient.makeAuthHeader(token)
                    val response = api.getEpisodes(authHeader, itemId, settings.jellyfinUserId)
                    seriesEpisodes = response.items.sortedWith(compareBy({ it.parentIndexNumber ?: 1 }, { it.indexNumber ?: 1 }))
                } catch (e: Exception) {
                    android.util.Log.e("PlayerScreen", "Error loading episodes list", e)
                }
            } else {
                seriesEpisodes = listOf(
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
            }
        }
    }

    var currentProgressPercent by remember { mutableStateOf(0) }
    var remainingMinutes by remember { mutableStateOf(0) }

    fun saveProgressAndExit() {
        viewModel.updatePlaybackProgress(item.id, currentProgressPercent, remainingMinutes)
        viewModel.navigateTo(Screen.Detail(item.id))
    }

    fun getNextEpisodeId(): String? {
        if (!item.isSeries) return null
        val currentEpId = episodeId ?: seriesEpisodes.firstOrNull()?.id ?: "mock_ep1"
        val cleanCurrentEpId = currentEpId.removePrefix("jellyfin_")
        val currentIndex = seriesEpisodes.indexOfFirst {
            it.id.equals(currentEpId, ignoreCase = true) ||
            it.id.removePrefix("jellyfin_").equals(cleanCurrentEpId, ignoreCase = true)
        }
        if (currentIndex != -1 && currentIndex + 1 < seriesEpisodes.size) {
            return seriesEpisodes[currentIndex + 1].id
        }
        return null
    }

    fun getPreviousEpisodeId(): String? {
        if (!item.isSeries) return null
        val currentEpId = episodeId ?: seriesEpisodes.firstOrNull()?.id ?: "mock_ep1"
        val cleanCurrentEpId = currentEpId.removePrefix("jellyfin_")
        val currentIndex = seriesEpisodes.indexOfFirst {
            it.id.equals(currentEpId, ignoreCase = true) ||
            it.id.removePrefix("jellyfin_").equals(cleanCurrentEpId, ignoreCase = true)
        }
        if (currentIndex > 0) {
            return seriesEpisodes[currentIndex - 1].id
        }
        return null
    }

    fun getNextMusicId(): String? {
        if (!isAudio) return null
        val currentIndex = musicPlaylist.indexOfFirst { it.id == item.id }
        if (currentIndex != -1 && currentIndex + 1 < musicPlaylist.size) {
            return musicPlaylist[currentIndex + 1].id
        } else if (musicPlaylist.isNotEmpty()) {
            return musicPlaylist.first().id
        }
        return null
    }

    fun playPrevious() {
        if (isAudio) {
            val currentIndex = musicPlaylist.indexOfFirst { it.id == item.id }
            if (currentIndex > 0) {
                viewModel.navigateTo(Screen.Player(musicPlaylist[currentIndex - 1].id))
            } else if (musicPlaylist.isNotEmpty()) {
                viewModel.navigateTo(Screen.Player(musicPlaylist.last().id))
            }
        } else if (item.isSeries) {
            val prevEpId = getPreviousEpisodeId()
            if (prevEpId != null) {
                viewModel.navigateTo(Screen.Player(item.id, prevEpId))
            }
        }
    }

    fun playNext() {
        if (isAudio) {
            val nextMusicId = getNextMusicId()
            if (nextMusicId != null) {
                viewModel.navigateTo(Screen.Player(nextMusicId))
            } else {
                saveProgressAndExit()
            }
        } else if (item.isSeries) {
            if (seriesEpisodes.isEmpty() && item.id.startsWith("jellyfin_") && settings.jellyfinServerUrl.isNotEmpty()) {
                coroutineScope.launch {
                    try {
                        val itemId = item.id.removePrefix("jellyfin_")
                        val cleanUrl = if (settings.jellyfinServerUrl.endsWith("/")) settings.jellyfinServerUrl.dropLast(1) else settings.jellyfinServerUrl
                        val token = settings.jellyfinToken
                        val api = com.example.data.jellyfin.JellyfinClient.getApi(cleanUrl)
                        val authHeader = com.example.data.jellyfin.JellyfinClient.makeAuthHeader(token)
                        val response = api.getEpisodes(authHeader, itemId, settings.jellyfinUserId)
                        seriesEpisodes = response.items.sortedWith(compareBy({ it.parentIndexNumber ?: 1 }, { it.indexNumber ?: 1 }))
                        
                        val nextEpId = getNextEpisodeId()
                        if (nextEpId != null) {
                            viewModel.navigateTo(Screen.Player(item.id, nextEpId))
                        } else {
                            saveProgressAndExit()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("PlayerScreen", "Error playing next episode dynamically", e)
                        saveProgressAndExit()
                    }
                }
            } else {
                val nextEpId = getNextEpisodeId()
                if (nextEpId != null) {
                    viewModel.navigateTo(Screen.Player(item.id, nextEpId))
                } else {
                    saveProgressAndExit()
                }
            }
        }
    }

    var activePlaybackUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(item, settings, episodeId) {
        if (item.id.startsWith("jellyfin_") && settings.jellyfinServerUrl.isNotEmpty()) {
            val itemId = item.id.removePrefix("jellyfin_")
            val cleanUrl = if (settings.jellyfinServerUrl.endsWith("/")) settings.jellyfinServerUrl.dropLast(1) else settings.jellyfinServerUrl
            val token = settings.jellyfinToken
            if (isAudio) {
                activePlaybackUrl = "$cleanUrl/Audio/$itemId/stream?static=true&api_key=$token"
            } else if (item.isSeries) {
                if (episodeId != null) {
                    activePlaybackUrl = "$cleanUrl/Videos/$episodeId/stream?static=true&api_key=$token"
                } else {
                    try {
                        val api = com.example.data.jellyfin.JellyfinClient.getApi(cleanUrl)
                        val authHeader = com.example.data.jellyfin.JellyfinClient.makeAuthHeader(token)
                        val episodesResponse = api.getEpisodes(authHeader, itemId, settings.jellyfinUserId)
                        val firstEpisodeId = episodesResponse.items.firstOrNull()?.id
                        if (firstEpisodeId != null) {
                            activePlaybackUrl = "$cleanUrl/Videos/$firstEpisodeId/stream?static=true&api_key=$token"
                        } else {
                            activePlaybackUrl = "$cleanUrl/Videos/$itemId/stream?static=true&api_key=$token"
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("PlayerScreen", "Error fetching episodes, using fallback", e)
                        activePlaybackUrl = "$cleanUrl/Videos/$itemId/stream?static=true&api_key=$token"
                    }
                }
            } else {
                activePlaybackUrl = "$cleanUrl/Videos/$itemId/stream?static=true&api_key=$token"
            }
        } else {
            if (isAudio) {
                activePlaybackUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            } else {
                if (item.isSeries && episodeId != null) {
                    activePlaybackUrl = when (episodeId) {
                        "mock_ep1" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                        "mock_ep2" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
                        "mock_ep3" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
                        else -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                    }
                } else {
                    activePlaybackUrl = when (item.id) {
                        "dark_knight" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                        "neon_nexus" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
                        "dune_arrakis" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
                        else -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                    }
                }
            }
        }
    }

    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(if (isAudio) C.AUDIO_CONTENT_TYPE_MUSIC else C.AUDIO_CONTENT_TYPE_MOVIE)
                .build()
            setAudioAttributes(audioAttributes, true)
            volume = 1f // Guarantee full output sound
            playWhenReady = true
        }
    }

    // Initialize secondary muted ExoPlayer for mini preview seek in real-time
    val previewExoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            volume = 0f
            playWhenReady = false
        }
    }

    DisposableEffect(previewExoPlayer) {
        onDispose {
            previewExoPlayer.release()
        }
    }

    var isPlaying by remember { mutableStateOf(true) }
    var durationMs by remember { mutableStateOf(0L) }
    var positionMs by remember { mutableStateOf(0L) }
    var playerVolume by remember { mutableStateOf(1f) }

    // Toggle popups
    var showSubtitleDialog by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }

    val activeSubtitleLanguage by viewModel.subtitleLanguage.collectAsState()
    val activeSubtitleFontSize by viewModel.subtitleFontSize.collectAsState()

    // Sync ExoPlayer state and tracking duration
    val listener = remember(item, episodeId, seriesEpisodes) {
        object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingChanged: Boolean) {
                isPlaying = isPlayingChanged
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    durationMs = exoPlayer.duration
                } else if (state == Player.STATE_ENDED) {
                    playNext()
                }
            }
        }
    }

    DisposableEffect(exoPlayer, listener) {
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(activePlaybackUrl) {
        if (!activePlaybackUrl.isNullOrEmpty()) {
            positionMs = 0L
            currentProgressPercent = 0
            remainingMinutes = 0
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            val media3Item = Media3Item.fromUri(activePlaybackUrl!!)
            exoPlayer.setMediaItem(media3Item)
            exoPlayer.prepare()
            exoPlayer.play()
            isPlaying = true

            // Set up previewExoPlayer on the same video URL synchronously (muted)
            previewExoPlayer.stop()
            previewExoPlayer.clearMediaItems()
            previewExoPlayer.setMediaItem(media3Item)
            previewExoPlayer.prepare()
        }
    }

    // Progress updates loop
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                positionMs = exoPlayer.currentPosition
                durationMs = if (exoPlayer.duration > 0) exoPlayer.duration else 1L
                if (durationMs > 1) {
                    currentProgressPercent = ((positionMs * 100) / durationMs).toInt()
                    val remainingMs = durationMs - positionMs
                    remainingMinutes = (remainingMs / 1000 / 60).toInt()
                }
                delay(500)
            }
        }
    }

    // Auto-hide controls effect
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(4000)
            controlsVisible = false
        }
    }

    LaunchedEffect(isInPipMode) {
        if (isInPipMode) {
            controlsVisible = false
        }
    }

    fun formatTime(ms: Long): String {
        val totalSecs = ms / 1000
        val hours = totalSecs / 3600
        val minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    val glassModifier = if (settings.glassmorphismEnabled) {
        Modifier
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
    } else {
        Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { controlsVisible = !controlsVisible })
            }
    ) {
        // High def movie scene simulated atmosphere / Video rendering
        if (isAudio) {
            // Audio mode - beautiful visual album disc art
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF5D5DFF).copy(alpha = 0.25f),
                                Color(0xFF7D01B1).copy(alpha = 0.20f),
                                Color.Black
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray)
                            .border(6.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Playing Music",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(110.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.director,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Video mode - Native AndroidView PlayerView surface
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                        setBackgroundColor(android.graphics.Color.BLACK)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }



        // 1. IMMERSIVE TOP HEADER BAR
        if (!isInPipMode) {
            AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Transparent
                             )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.clickable { saveProgressAndExit() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Exit player",
                                tint = Color.White
                            )
                        }

                        Column {
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val currentEpisode = remember(episodeId, seriesEpisodes) {
                                val currentEpId = episodeId ?: seriesEpisodes.firstOrNull()?.id ?: "mock_ep1"
                                val cleanCurrentEpId = currentEpId.removePrefix("jellyfin_")
                                seriesEpisodes.find {
                                    it.id.equals(currentEpId, ignoreCase = true) ||
                                    it.id.removePrefix("jellyfin_").equals(cleanCurrentEpId, ignoreCase = true)
                                }
                            }
                            Text(
                                text = if (item.isSeries && currentEpisode != null) {
                                    val seasonStr = currentEpisode.parentIndexNumber?.let { "T$it" } ?: "T1"
                                    val epStr = currentEpisode.indexNumber?.let { "E$it" } ?: ""
                                    val nameStr = currentEpisode.name.takeIf { it.isNotEmpty() }?.let { " - $it" } ?: ""
                                    "$seasonStr:$epStr$nameStr"
                                } else {
                                    item.detailsSubtitle
                                },
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        IconButton(
                            onClick = {},
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                        }
                    }
                }
            }
        }

        // 2. CENTRAL PLAYER ACTION BUTTONS
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Skip Previous
                if (item.isSeries || isAudio) {
                    val hasPrev = remember(episodeId, seriesEpisodes, musicPlaylist) {
                        if (isAudio) {
                            val idx = musicPlaylist.indexOfFirst { it.id == item.id }
                            idx > 0 || musicPlaylist.isNotEmpty()
                        } else {
                            getPreviousEpisodeId() != null
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (hasPrev) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.02f))
                            .border(1.dp, if (hasPrev) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f), CircleShape)
                            .clickable(enabled = hasPrev) {
                                playPrevious()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Anterior",
                            tint = if (hasPrev) Color.White else Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Rewind 10s
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .clickable {
                            val targetPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
                            exoPlayer.seekTo(targetPos)
                            positionMs = targetPos
                            previewExoPlayer.seekTo(targetPos)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Replay10, contentDescription = "Voltar 10s", tint = Color.White, modifier = Modifier.size(28.dp))
                }

                // Play / Pause Circle
                val scale by animateFloatAsState(
                    targetValue = if (isPlaying) 0.9f else 1.1f,
                    animationSpec = spring(dampingRatio = 0.5f)
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                        .clickable {
                            if (isPlaying) {
                                exoPlayer.pause()
                            } else {
                                exoPlayer.play()
                            }
                            isPlaying = !isPlaying
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Reprodutor",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(42.dp)
                    )
                }

                // Fastforward 10s
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .clickable {
                            val targetPos = (exoPlayer.currentPosition + 10000).coerceAtMost(durationMs)
                            exoPlayer.seekTo(targetPos)
                            positionMs = targetPos
                            previewExoPlayer.seekTo(targetPos)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Forward10, contentDescription = "Avançar 10s", tint = Color.White, modifier = Modifier.size(28.dp))
                }

                // Skip Next
                if (item.isSeries || isAudio) {
                    val hasNext = remember(episodeId, seriesEpisodes, musicPlaylist) {
                        if (isAudio) {
                            val idx = musicPlaylist.indexOfFirst { it.id == item.id }
                            idx < musicPlaylist.size - 1 || musicPlaylist.isNotEmpty()
                        } else {
                            getNextEpisodeId() != null
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (hasNext) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.02f))
                            .border(1.dp, if (hasNext) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f), CircleShape)
                            .clickable(enabled = hasNext) {
                                playNext()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Próximo",
                            tint = if (hasNext) Color.White else Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 3. BOTTOM CONTROLS AND SCRUBBER
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Progress Slider
                    Column {
                        if (!isAudio) {
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                val containerWidth = maxWidth
                                val progressFraction = currentProgressPercent / 100f
                                val cardWidth = 140.dp
                                val previewHeight = 84.dp

                                val relativeThumbX = remember(progressFraction, containerWidth) {
                                    val thumbX = containerWidth * progressFraction
                                    val rawLeft = thumbX - (cardWidth / 2)
                                    val clampedLeft = rawLeft.coerceIn(0.dp, maxOf(0.dp, containerWidth - cardWidth))
                                    thumbX - clampedLeft
                                }

                                val thumbOffset = remember(progressFraction, containerWidth) {
                                    val thumbX = containerWidth * progressFraction
                                    val rawLeft = thumbX - (cardWidth / 2)
                                    rawLeft.coerceIn(0.dp, maxOf(0.dp, containerWidth - cardWidth))
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(cardWidth)
                                        .offset(x = thumbOffset)
                                ) {
                                    // Video Preview Container
                                    Card(
                                        modifier = Modifier
                                            .width(cardWidth)
                                            .height(previewHeight),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                        colors = CardDefaults.cardColors(containerColor = Color.Black)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            if (!activePlaybackUrl.isNullOrEmpty()) {
                                                AndroidView(
                                                    factory = { ctx ->
                                                        PlayerView(ctx).apply {
                                                            player = previewExoPlayer
                                                            useController = false
                                                            resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                                            setBackgroundColor(android.graphics.Color.BLACK)
                                                        }
                                                    },
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            Brush.linearGradient(
                                                                colors = listOf(Color(0xFF1F1F2F), Color(0xFF0F0F1A))
                                                            )
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayCircle,
                                                        contentDescription = "Trailer",
                                                        tint = Color.White.copy(alpha = 0.4f),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }

                                            // Pulse red badge for active scene trailer/prêvia
                                            Row(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(4.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color.Black.copy(alpha = 0.65f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.Red)
                                                )
                                                Text(
                                                    text = "PRÉVIA EM TEMPO REAL",
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // Play icon decoration
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Black.copy(alpha = 0.5f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Play",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    // White styled timestamp banner (just like in the screenshot)
                                    Box(
                                        modifier = Modifier
                                            .width(82.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.White)
                                            .padding(vertical = 4.dp, horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val seekTime = (progressFraction * durationMs).toLong()
                                        Text(
                                            text = formatTime(if (seekTime > 0) seekTime else positionMs),
                                            color = Color.Black,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Downward pointing arrow exactly aligned with current progress thumb
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                    ) {
                                        Canvas(
                                            modifier = Modifier
                                                .size(12.dp, 6.dp)
                                                .offset(x = relativeThumbX - 6.dp)
                                        ) {
                                            val path = Path().apply {
                                                moveTo(0f, 0f)
                                                lineTo(size.width, 0f)
                                                lineTo(size.width / 2f, size.height)
                                                close()
                                            }
                                            drawPath(path, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        Slider(
                            value = currentProgressPercent.toFloat(),
                            onValueChange = { percent ->
                                currentProgressPercent = percent.toInt()
                                if (durationMs > 1) {
                                    val seekPos = (percent * durationMs) / 100
                                    exoPlayer.seekTo(seekPos.toLong())
                                    positionMs = seekPos.toLong()
                                    previewExoPlayer.seekTo(seekPos.toLong())
                                }
                            },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTime(positionMs),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatTime(durationMs),
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Bottom control buttons bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.width(130.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        playerVolume = if (playerVolume > 0f) 0f else 1f
                                        exoPlayer.volume = playerVolume
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (playerVolume == 0f) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                        contentDescription = "Volume",
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Slider(
                                    value = playerVolume,
                                    onValueChange = { vol ->
                                        playerVolume = vol
                                        exoPlayer.volume = vol
                                    },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.clickable { playNext() }
                            ) {
                                Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White.copy(alpha = 0.7f))
                                Text("Próximo", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Auxiliary action buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            IconButton(
                                onClick = {
                                    if (android.os.Build.VERSION.SDK_INT >= 26) {
                                        try {
                                            activity?.enterPictureInPictureMode(
                                                android.app.PictureInPictureParams.Builder().build()
                                            )
                                        } catch (e: Exception) {
                                            android.util.Log.e("PlayerScreen", "Error entering PiP", e)
                                        }
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Default.BrandingWatermark, contentDescription = "Picture in picture", tint = Color.White)
                            }
                            IconButton(onClick = {}) {
                                Icon(imageVector = Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = Color.White)
                            }
                        }
                    }
                }
            }
            }
        }

        // 4. FLOATING SETTINGS SUITE POPOVER (Subtitles Choice dialog)
        if (showSubtitleDialog) {
            AlertDialog(
                onDismissRequest = { showSubtitleDialog = false },
                title = {
                    Text(
                        text = "Configurações de Legendas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Column {
                            Text(
                                "Idioma da Legenda",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val subLanguages = listOf("Português (Brasil)", "Inglês (CC)", "Espanhol", "Desativar")
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                subLanguages.chunked(2).forEach { row ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        row.forEach { lang ->
                                            val isSelected = activeSubtitleLanguage == lang
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White.copy(alpha = 0.05f))
                                                    .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                                    .clickable { viewModel.updateSubtitleLanguage(lang) }
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = lang,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val fontSizeLabel = "A${activeSubtitleFontSize.toInt()}"
                                Text("Tamanho do Texto", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(fontSizeLabel, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Slider(
                                value = activeSubtitleFontSize,
                                onValueChange = { viewModel.updateSubtitleFontSize(it) },
                                valueRange = 1f..4f,
                                steps = 2,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showSubtitleDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Aplicar Alterações", fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color(0xFF1F1F24),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
