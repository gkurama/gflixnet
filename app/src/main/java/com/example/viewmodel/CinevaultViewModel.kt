package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CinevaultDatabase
import com.example.data.CinevaultRepository
import com.example.model.MediaItem
import com.example.model.UserSetting
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log

sealed class Screen {
    object Splash : Screen()
    object Home : Screen()
    data class Detail(val mediaId: String) : Screen()
    data class Player(val mediaId: String, val episodeId: String? = null) : Screen()
    object Settings : Screen()
    object Search : Screen()
    object Downloads : Screen()
}

sealed class JellyfinSyncState {
    object Idle : JellyfinSyncState()
    object Connecting : JellyfinSyncState()
    data class Success(val itemsCount: Int) : JellyfinSyncState()
    data class Error(val message: String) : JellyfinSyncState()
}

class CinevaultViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CinevaultRepository
    
    // Core database states
    val allMediaItems: StateFlow<List<MediaItem>>
    val settingsState: StateFlow<UserSetting>

    // Jellyfin states
    private val _jellyfinSyncState = MutableStateFlow<JellyfinSyncState>(JellyfinSyncState.Idle)
    val jellyfinSyncState: StateFlow<JellyfinSyncState> = _jellyfinSyncState.asStateFlow()

    // UI Interactive States
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow("Tudo") // "Tudo", "Filmes", "Séries de TV", "Favoritos", "Música"
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private val _activeHomeMode = MutableStateFlow("home") // "home", "filmes", "series", "musica"
    val activeHomeMode: StateFlow<String> = _activeHomeMode.asStateFlow()

    private val _isInPipMode = MutableStateFlow(false)
    val isInPipMode: StateFlow<Boolean> = _isInPipMode.asStateFlow()

    fun setIsInPipMode(inPip: Boolean) {
        _isInPipMode.value = inPip
    }

    fun setActiveHomeMode(mode: String) {
        _activeHomeMode.value = mode
    }

    init {
        val database = CinevaultDatabase.getDatabase(application)
        repository = CinevaultRepository(database.dao())
        
        allMediaItems = repository.allMediaItems
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        settingsState = repository.userSettings
            .combine(MutableStateFlow(UserSetting())) { custom, default ->
                custom ?: default
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UserSetting()
            )

        // Run database seed in background
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }

        // Auto-connect and auto-sync Gflixnet servers silently on app start
        viewModelScope.launch {
            delay(1500) // allow repository settings and database to fully load
            val current = settingsState.value
            if (!current.jellyfinIsConnected || current.jellyfinUsername != "gflixnet" || current.jellyfinServerUrl != "http://www.gflixnet.com") {
                Log.d("CinevaultViewModel", "Startup: Auto-connecting to Gflixnet server...")
                connectAndSyncJellyfin("http://www.gflixnet.com", "gflixnet", "")
            } else {
                Log.d("CinevaultViewModel", "Startup: Silent background sync Gflixnet server...")
                connectAndSyncJellyfin("http://www.gflixnet.com", "gflixnet", "")
            }
        }
    }

    // Navigation and screen management
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setTab(tab: String) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Toggle Watchlist
    fun toggleWatchlist(mediaId: String) {
        viewModelScope.launch {
            val item = repository.getMediaItemById(mediaId)
            if (item != null) {
                val updated = item.copy(inWatchlist = !item.inWatchlist)
                repository.updateMediaItem(updated)
            }
        }
    }

    // Toggle Favorite
    fun toggleFavorite(mediaId: String) {
        viewModelScope.launch {
            val item = repository.getMediaItemById(mediaId)
            if (item != null) {
                val updated = item.copy(isFavorite = !item.isFavorite)
                repository.updateMediaItem(updated)
            }
        }
    }

    // Playback and progress interactions
    fun updatePlaybackProgress(mediaId: String, progressPercent: Int, remainingMinutes: Int) {
        viewModelScope.launch {
            val item = repository.getMediaItemById(mediaId)
            if (item != null) {
                val updated = item.copy(
                    userProgressPercent = progressPercent,
                    userRemainingMinutes = remainingMinutes,
                    lastWatchedTimestamp = System.currentTimeMillis()
                )
                repository.updateMediaItem(updated)
            }
        }
    }

    fun clearPlaybackProgress(mediaId: String) {
        viewModelScope.launch {
            val item = repository.getMediaItemById(mediaId)
            if (item != null) {
                val updated = item.copy(
                    userProgressPercent = 0,
                    userRemainingMinutes = 0,
                    lastWatchedTimestamp = 0L
                )
                repository.updateMediaItem(updated)
            }
        }
    }

    // Settings adjustments
    fun updateMetadataSync(enabled: Boolean) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateSettings(current.copy(metadataSyncEnabled = enabled))
        }
    }

    fun updateStreamingQuality(quality: String) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateSettings(current.copy(remoteStreamingQuality = quality))
        }
    }

    fun updateAudioPassthrough(enabled: Boolean) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateSettings(current.copy(audioPassthroughEnabled = enabled))
        }
    }

    fun updateGlassmorphism(enabled: Boolean) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateSettings(current.copy(glassmorphismEnabled = enabled))
        }
    }

    fun updateInterfaceTheme(theme: String) {
        viewModelScope.launch {
            val current = settingsState.value
            repository.updateSettings(current.copy(interfaceTheme = theme))
        }
    }

    // Subtitle configurations
    private val _subtitleLanguage = MutableStateFlow("Português (Brasil)")
    val subtitleLanguage: StateFlow<String> = _subtitleLanguage.asStateFlow()

    private val _subtitleFontSize = MutableStateFlow(2f) // range 1f..4f
    val subtitleFontSize: StateFlow<Float> = _subtitleFontSize.asStateFlow()

    fun updateSubtitleLanguage(lang: String) {
        _subtitleLanguage.value = lang
    }

    fun updateSubtitleFontSize(size: Float) {
        _subtitleFontSize.value = size
    }

    fun connectAndSyncJellyfin(serverUrl: String, username: String, pass: String) {
        viewModelScope.launch {
            _jellyfinSyncState.value = JellyfinSyncState.Connecting
            try {
                if (serverUrl.isBlank()) {
                    _jellyfinSyncState.value = JellyfinSyncState.Error("A URL do servidor Jellyfin não pode estar vazia.")
                    return@launch
                }
                
                val cleanServerUrl = if (serverUrl.startsWith("http://") || serverUrl.startsWith("https://")) {
                    serverUrl
                } else {
                    "http://$serverUrl"
                }

                val current = settingsState.value
                val token: String
                val userId: String

                if (pass.isBlank() && current.jellyfinIsConnected && current.jellyfinToken.isNotEmpty() && current.jellyfinUserId.isNotEmpty()) {
                    token = current.jellyfinToken
                    userId = current.jellyfinUserId
                } else {
                    // Call client logic
                    val authResponse = com.example.data.jellyfin.JellyfinClient.authenticate(cleanServerUrl, username, pass)
                    token = authResponse.accessToken
                    userId = authResponse.user.id
                }
                
                // Fetch library items
                val items = com.example.data.jellyfin.JellyfinClient.fetchAndMapItems(cleanServerUrl, userId, token)
                
                // Insert items in DB
                val database = CinevaultDatabase.getDatabase(getApplication())
                database.dao().insertMediaItems(items)
                
                // Save settings state
                val updated = current.copy(
                    jellyfinServerUrl = cleanServerUrl,
                    jellyfinUsername = username,
                    jellyfinToken = token,
                    jellyfinUserId = userId,
                    jellyfinIsConnected = true,
                    jellyfinLastSyncTimestamp = System.currentTimeMillis()
                )
                repository.updateSettings(updated)
                
                _jellyfinSyncState.value = JellyfinSyncState.Success(items.size)
            } catch (e: Exception) {
                Log.e("CinevaultViewModel", "Jellyfin connection error", e)
                _jellyfinSyncState.value = JellyfinSyncState.Error(
                    e.localizedMessage ?: "Erro ao conectar com o servidor Jellyfin. Por favor, verifique o URL e credenciais."
                )
            }
        }
    }

    fun disconnectJellyfin() {
        viewModelScope.launch {
            _jellyfinSyncState.value = JellyfinSyncState.Idle
            val current = settingsState.value
            val updated = current.copy(
                jellyfinServerUrl = "",
                jellyfinUsername = "",
                jellyfinToken = "",
                jellyfinUserId = "",
                jellyfinIsConnected = false,
                jellyfinLastSyncTimestamp = 0L
            )
            repository.updateSettings(updated)
        }
    }
}
