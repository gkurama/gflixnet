package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CinevaultViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: CinevaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            MyApplicationTheme {
                CinevaultAppShell(viewModel)
            }
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: android.content.res.Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        viewModel.setIsInPipMode(isInPictureInPictureMode)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinevaultAppShell(viewModel: CinevaultViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val settings by viewModel.settingsState.collectAsState()
    val activeHomeMode by viewModel.activeHomeMode.collectAsState()
    val jellyfinState by viewModel.jellyfinSyncState.collectAsState()

    if (currentScreen is Screen.Splash) {
        SplashScreen(
            onSplashComplete = {
                viewModel.navigateTo(Screen.Home)
            }
        )
        return
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Gracefully handle hardware & system gesture back presses
    BackHandler(enabled = currentScreen !is Screen.Home || activeHomeMode != "home" || drawerState.isOpen) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    if (activeHomeMode != "home") {
                        viewModel.setActiveHomeMode("home")
                    }
                }
                is Screen.Detail -> {
                    viewModel.navigateTo(Screen.Home)
                }
                is Screen.Player -> {
                    viewModel.navigateTo(Screen.Detail(screen.mediaId))
                }
                else -> {
                    viewModel.navigateTo(Screen.Home)
                }
            }
        }
    }

    val glassBackground = if (settings.glassmorphismEnabled) {
        Color.White.copy(alpha = 0.05f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentScreen !is Screen.Player && currentScreen !is Screen.Detail,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                drawerContentColor = MaterialTheme.colorScheme.onBackground
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Branding Logo inside Navigation Drawer
                Image(
                    painter = painterResource(id = R.drawable.gflixnet_text_logo_1779485419941),
                    contentDescription = "Logo Gflixnet",
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(110.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.FillHeight
                )
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 8.dp))
                Spacer(modifier = Modifier.height(16.dp))
                
                val menuItems = listOf(
                    BottomNavItem("Início", Screen.Home, Icons.Default.Home, "inicio"),
                    BottomNavItem("Busca", Screen.Search, Icons.Default.Search, "busca"),
                    BottomNavItem("Downloads", Screen.Downloads, Icons.Default.Download, "downloads"),
                    BottomNavItem("Perfil", Screen.Settings, Icons.Default.Person, "perfil")
                )
                
                menuItems.forEach { item ->
                    val isSelected = when (item.screen) {
                        Screen.Home -> currentScreen is Screen.Home
                        Screen.Search -> currentScreen is Screen.Search
                        Screen.Downloads -> currentScreen is Screen.Downloads
                        Screen.Settings -> currentScreen is Screen.Settings
                        else -> false
                    }
                    
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                                fontSize = 15.sp
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            scope.launch { drawerState.close() }
                            viewModel.navigateTo(item.screen)
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .testTag("drawer_item_${item.tag}")
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Modern, integrated Gflixnet Cloud Connection Card (Três traços lateral)
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (settings.jellyfinIsConnected) Color(0xFF10B981).copy(alpha = 0.15f)
                                        else Color.White.copy(alpha = 0.08f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (settings.jellyfinIsConnected) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = if (settings.jellyfinIsConnected) Color(0xFF10B981) else Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (settings.jellyfinIsConnected) "Nuvem Conectada" else "Nuvem Desconectada",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (settings.jellyfinIsConnected) Color(0xFF10B981) else Color.White.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = if (settings.jellyfinIsConnected) "Login Permanente Ativo" else "Clique para conectar",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (settings.jellyfinIsConnected) {
                            if (settings.jellyfinLastSyncTimestamp > 0) {
                                val formattedDate = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                                    .format(java.util.Date(settings.jellyfinLastSyncTimestamp))
                                Text(
                                    text = "Sincronizado em: $formattedDate",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.connectAndSyncJellyfin(
                                            settings.jellyfinServerUrl,
                                            settings.jellyfinUsername,
                                            ""
                                        )
                                    },
                                    modifier = Modifier.weight(1.4f).height(34.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sincronizar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.disconnectJellyfin() },
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("Sair", fontSize = 10.sp)
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.connectAndSyncJellyfin("http://www.gflixnet.com", "gflixnet", "")
                                },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Conectar Gflixnet", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Sync State feedbacks
                        when (jellyfinState) {
                            is com.example.viewmodel.JellyfinSyncState.Connecting -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 1.5.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("Sincronizando...", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            is com.example.viewmodel.JellyfinSyncState.Success -> {
                                val count = (jellyfinState as com.example.viewmodel.JellyfinSyncState.Success).itemsCount
                                Text(
                                    text = "Sincronizados $count itens",
                                    fontSize = 10.sp,
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            is com.example.viewmodel.JellyfinSyncState.Error -> {
                                Text(
                                    text = "Falha na sincronização",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            else -> {}
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                // Only show TopAppBar on Home, Search, Downloads, Settings
                if (currentScreen !is Screen.Player && currentScreen !is Screen.Detail) {
                    Surface(
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .statusBarsPadding()
                                .height(84.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                        ) {
                            // Left section (Navigation actions & Branding)
                            Row(
                                modifier = Modifier.align(Alignment.CenterStart),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (currentScreen is Screen.Home && activeHomeMode != "home") {
                                    // Dynamic Sub-menu header with Arrow & Home icon (Screens 2 and 3)
                                    IconButton(onClick = { viewModel.setActiveHomeMode("home") }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Voltar",
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    IconButton(onClick = { viewModel.setActiveHomeMode("home") }) {
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = "Início",
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = when (activeHomeMode) {
                                            "filmes" -> "Filmes"
                                            "series" -> "Séries"
                                            "filmes_biblicos" -> "Filmes Bíblicos"
                                            "series_biblicas" -> "Séries Bíblicas"
                                            else -> "Músicas"
                                        },
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                } else {
                                    // Default Menu and Branding
                                    val showHamburger = currentScreen is Screen.Home || currentScreen is Screen.Search || currentScreen is Screen.Downloads || currentScreen is Screen.Settings
                                    if (showHamburger) {
                                        IconButton(onClick = {
                                            scope.launch {
                                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = "Menu",
                                                tint = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    } else {
                                        IconButton(onClick = { viewModel.navigateTo(Screen.Home) }) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowBack,
                                                contentDescription = "Voltar",
                                                tint = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    }
                                    
                                    if (currentScreen !is Screen.Home) {
                                        // Title of non-Home screens
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (currentScreen is Screen.Settings || currentScreen is Screen.Search) {
                                                IconButton(onClick = {
                                                    viewModel.setActiveHomeMode("home")
                                                    viewModel.navigateTo(Screen.Home)
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Home,
                                                        contentDescription = "Início",
                                                        tint = MaterialTheme.colorScheme.onBackground
                                                    )
                                                }
                                            }
                                            Text(
                                                text = when(currentScreen) {
                                                    Screen.Settings -> "Configurações"
                                                    Screen.Search -> "Busca"
                                                    Screen.Downloads -> "Downloads"
                                                    else -> "Gflixnet"
                                                },
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Perfect Centered and Enlarged Logo
                            if (currentScreen is Screen.Home && activeHomeMode == "home") {
                                Image(
                                    painter = painterResource(id = R.drawable.gflixnet_text_logo_1779485419941),
                                    contentDescription = "Logo Gflixnet",
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .height(84.dp)
                                        .padding(vertical = 2.dp),
                                    contentScale = androidx.compose.ui.layout.ContentScale.FillHeight
                                )
                            }
                            
                            // Right section (Sync status + actions: Search, Profile)
                            Row(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Search icon
                                IconButton(onClick = { viewModel.navigateTo(Screen.Search) }) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Buscar",
                                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(4.dp))
                                
                                // User Profile Avatar
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.secondary
                                                )
                                            )
                                        )
                                        .clickable { viewModel.navigateTo(Screen.Settings) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "U",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            // Handle transitions between layouts smoothly
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (val screen = currentScreen) {
                    is Screen.Home -> {
                        HomeScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                        )
                    }
                    is Screen.Detail -> {
                        DetailScreen(
                            mediaId = screen.mediaId,
                            viewModel = viewModel
                        )
                    }
                    is Screen.Player -> {
                        PlayerScreen(
                            mediaId = screen.mediaId,
                            episodeId = screen.episodeId,
                            viewModel = viewModel
                        )
                    }
                    is Screen.Settings -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                        )
                    }
                    is Screen.Search -> {
                        SearchScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                        )
                    }
                    is Screen.Downloads -> {
                        DownloadsScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                        )
                    }
                    Screen.Splash -> {
                        // Handled separately on launch
                    }
                }
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val screen: Screen,
    val icon: ImageVector,
    val tag: String
)
