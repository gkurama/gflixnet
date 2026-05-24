// Gflixnet Web Core Application Engine

// Global State
let currentServerUrl = localStorage.getItem('gflixnet_server_url') || '';
let currentUserId = localStorage.getItem('gflixnet_user_id') || '';
let currentToken = localStorage.getItem('gflixnet_token') || '';
let isDemoMode = localStorage.getItem('gflixnet_demo_mode') === 'true';

let allMediaItems = [];
let currentCategory = 'all';
let currentSelectedMedia = null;
let heroMediaItem = null;

// Mock / Demo Media Data
const demoMediaData = [
    {
        id: "demo_1",
        title: "Big Buck Bunny",
        year: 2008,
        runtime: "10min",
        isSeries: false,
        detailsSubtitle: "Animação Blender",
        rating: "Livre",
        genres: "Animação, Comédia",
        tags: "Demo, 1080p, Grátis",
        synopsis: "Um coelho gigante e adorável chamado Bunny resolve dar uma lição em três roedores encrenqueiros da floresta que ousaram perturbar seu sossego e maltratar seus amigos borboletas. Um clássico curta-metragem livre da Blender Foundation.",
        director: "Sacha Goedegebure",
        writers: "Blender Institute",
        language: "Inglês",
        awards: "Open Movie Project",
        libraryName: "Filmes recomendados",
        posterUrl: "https://upload.wikimedia.org/wikipedia/commons/c/c5/Big_Buck_Bunny_Screen_01.png",
        backdropUrl: "https://upload.wikimedia.org/wikipedia/commons/c/c5/Big_Buck_Bunny_Screen_01.png",
        streamUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    },
    {
        id: "demo_2",
        title: "Tears of Steel",
        year: 2012,
        runtime: "12min",
        isSeries: false,
        detailsSubtitle: "Ficção Científica",
        rating: "12+",
        genres: "Ficção Científica, Ação",
        tags: "Cinevault, 4K",
        synopsis: "Ambientado em uma Amsterdã futurista, o curta retrata um grupo de cientistas que tenta salvar o mundo de robôs gigantes destrutivos reativando memórias de um relacionamento do passado. Efeitos visuais espetaculares produzidos em código aberto.",
        director: "Ian Hubert",
        writers: "Ton Roosendaal",
        language: "Inglês",
        awards: "Blender Foundation",
        libraryName: "Filmes recomendados",
        posterUrl: "https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=1200",
        backdropUrl: "https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=1200",
        streamUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
    },
    {
        id: "demo_3",
        title: "Sintel",
        year: 2010,
        runtime: "15min",
        isSeries: false,
        detailsSubtitle: "Fantasia Épica",
        rating: "14+",
        genres: "Fantasia, Aventura",
        tags: "Destaque, HDR",
        synopsis: "Sintel é uma jovem solitária que cuida de um pequeno dragão ferido chamado Scales. Quando Scales é raptado por um dragão muito maior, Sintel embarca em uma longa e solitária jornada em busca do seu amigo, descobrindo verdades surpreendentes.",
        director: "Colin Levy",
        writers: "Esther Wouda",
        language: "Holandês",
        awards: "Visual Fantasy Cup",
        libraryName: "Filmes recomendados",
        posterUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8f/Sintel_poster.jpg/800px-Sintel_poster.jpg",
        backdropUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8f/Sintel_poster.jpg/1280px-Sintel_poster.jpg",
        streamUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
    },
    {
        id: "demo_4",
        title: "Submersion Beats",
        year: 2023,
        runtime: "5min",
        isSeries: false,
        type: "Audio",
        detailsSubtitle: "Música Chillout",
        rating: "Livre",
        genres: "Instrumental, Lofi",
        tags: "Música, Relax",
        synopsis: "Uma trilha relaxante instrumental para acompanhar seu foco no ZimaOS ou servidor residencial. Sincronização impecável de som e canais de streaming.",
        director: "Soundhelix Waves",
        writers: "Independent Sound",
        language: "Instrumental",
        awards: "Gflixnet Beats",
        libraryName: "Músicas",
        posterUrl: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=600",
        backdropUrl: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=600",
        streamUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
    }
];

// Document Ready Initialization
document.addEventListener('DOMContentLoaded', () => {
    initApp();
});

function initApp() {
    // Fill Connection Modal fields with saved state
    if (currentServerUrl) {
        document.getElementById('serverUrl').value = currentServerUrl;
    }
    if (localStorage.getItem('gflixnet_username')) {
        document.getElementById('username').value = localStorage.getItem('gflixnet_username');
    }

    if (isDemoMode) {
        loadDemoMode();
    } else if (currentServerUrl && currentUserId && currentToken) {
        testAndLoadJellyfin();
    } else {
        // Automatically open Connection modal on first launch to assist configuration
        setTimeout(() => {
            openAuthModal();
        }, 800);
    }
}

// Global modal handling
window.openAuthModal = function() {
    document.getElementById('authModal').classList.add('active');
};

window.closeModal = function(modalId) {
    document.getElementById(modalId).classList.remove('active');
};

// Jellyfin REST API authenticate
async function handleAuthSubmit(event) {
    event.preventDefault();
    const loginBtn = document.getElementById('loginBtn');
    loginBtn.disabled = true;
    loginBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Conectando...';

    const serverUrlInput = document.getElementById('serverUrl').value.trim();
    const serverUrl = serverUrlInput.endsWith('/') ? serverUrlInput.slice(0, -1) : serverUrlInput;
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;

    const authHeader = 'MediaBrowser Client="Gflixnet", Device="WebBrowser", DeviceId="gflixnet_web_client", Version="1.0.0", Token=""';

    try {
        const response = await fetch(`${serverUrl}/Users/AuthenticateByName`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Emby-Authorization': authHeader
            },
            body: JSON.stringify({
                Username: username,
                Pw: password
            })
        });

        if (!response.ok) {
            throw new Error(`Código de erro HTTP: ${response.status}`);
        }

        const data = await response.json();
        
        // Save to Storage
        currentServerUrl = serverUrl;
        currentUserId = data.User.Id;
        currentToken = data.AccessToken;
        isDemoMode = false;

        localStorage.setItem('gflixnet_server_url', serverUrl);
        localStorage.setItem('gflixnet_user_id', data.User.Id);
        localStorage.setItem('gflixnet_token', data.AccessToken);
        localStorage.setItem('gflixnet_username', username);
        localStorage.setItem('gflixnet_demo_mode', 'false');

        closeModal('authModal');
        await fetchAndRenderJellyfinLibrary();

    } catch (error) {
        console.error('Jellyfin auth failed:', error);
        alert(`Falha de conexão com o Jellyfin:\n${error.message}\n\nPor favor, verifique se a URL do servidor está correta, acessível localmente e sem bloqueios de CORS.`);
    } finally {
        loginBtn.disabled = false;
        loginBtn.innerHTML = 'Conectar Conta <i class="fa-solid fa-arrow-right"></i>';
    }
}

// Mode: Demo Mode
window.useDemoMode = function() {
    isDemoMode = true;
    localStorage.setItem('gflixnet_demo_mode', 'true');
    closeModal('authModal');
    loadDemoMode();
};

function loadDemoMode() {
    // Update Indicators
    const indicator = document.getElementById('statusIndicator');
    indicator.className = 'status-circle online';
    indicator.style.backgroundColor = '#00D2FF';
    indicator.style.boxShadow = '0 0 8px #00D2FF';
    document.getElementById('statusText').innerText = 'Demonstração';

    allMediaItems = demoMediaData;
    renderMediaRows();
    
    // Choose first as hero
    setHeroBanner(allMediaItems[0]);
}

// Sincronizar via Jellyfin
async function testAndLoadJellyfin() {
    const indicator = document.getElementById('statusIndicator');
    const statusText = document.getElementById('statusText');

    indicator.className = 'status-circle online';
    statusText.innerText = 'Sincronizando...';

    const success = await fetchAndRenderJellyfinLibrary();
    if (!success) {
        indicator.className = 'status-circle offline';
        statusText.innerText = 'Erro Conexão';
    }
}

async function fetchAndRenderJellyfinLibrary() {
    const indicator = document.getElementById('statusIndicator');
    const statusText = document.getElementById('statusText');
    const authHeader = `MediaBrowser Client="Gflixnet", Device="WebBrowser", DeviceId="gflixnet_web_client", Version="1.0.0", Token="${currentToken}"`;

    try {
        // Fetch items recursive
        const response = await fetch(`${currentServerUrl}/Users/${currentUserId}/Items?IncludeItemTypes=Movie,Series,Audio&Recursive=true&Fields=PrimaryImageAspectRatio,BasicSyncInfo,Overview,Genres,ProductionYear,RunTimeTicks,OfficialRating,Studios`, {
            headers: {
                'X-Emby-Authorization': authHeader
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP Error Status: ${response.status}`);
        }

        const data = await response.json();
        
        // Map elements
        allMediaItems = data.Items.map(item => {
            const isSeries = item.Type === "Series";
            const isAudio = item.Type === "Audio";

            let runtimeString = "N/A";
            if (item.RunTimeTicks) {
                const totalSeconds = Math.floor(item.RunTimeTicks / 10000000);
                const minutes = Math.floor(totalSeconds / 60);
                const hrs = Math.floor(minutes / 60);
                const mins = minutes % 60;
                runtimeString = hrs > 0 ? `${hrs}h ${mins}min` : `${minutes}min`;
            }

            const posterUrl = item.ImageTags && item.ImageTags.Primary 
                ? `${currentServerUrl}/Items/${item.Id}/Images/Primary` 
                : 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=400';

            const backdropUrl = item.ImageTags && item.ImageTags.Primary 
                ? `${currentServerUrl}/Items/${item.Id}/Images/Primary` 
                : 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=1200';

            const streamUrl = isAudio 
                ? `${currentServerUrl}/Audio/${item.Id}/stream?api_key=${currentToken}`
                : `${currentServerUrl}/Videos/${item.Id}/stream?static=true&api_key=${currentToken}`;

            return {
                id: item.Id,
                title: item.Name,
                year: item.ProductionYear || 2024,
                runtime: runtimeString,
                isSeries: isSeries,
                type: item.Type,
                detailsSubtitle: isAudio ? "Musica" : (isSeries ? "Série de TV" : "Filme"),
                rating: item.OfficialRating || "Livre",
                genres: item.Genres ? item.Genres.join(", ") : "Geral",
                tags: isAudio ? "Jellyfin Audio" : "Jellyfin 1080p",
                synopsis: item.Overview || "Mídia hospedada no seu servidor Jellyfin.",
                director: item.Artists ? item.Artists.join(", ") : "Hospedagem Externa",
                writers: "Jellyfin Core",
                libraryName: isAudio ? "Músicas" : (isSeries ? "Séries de TV" : "Filmes"),
                posterUrl: posterUrl,
                backdropUrl: backdropUrl,
                streamUrl: streamUrl
            };
        });

        // Set indicator to online
        indicator.className = 'status-circle online';
        statusText.innerText = 'Online';

        if (allMediaItems.length > 0) {
            renderMediaRows();
            setHeroBanner(allMediaItems[0]);
        } else {
            document.getElementById('mediaLibrary').innerHTML = `
                <div class="loading-state">
                    <i class="fa-solid fa-folder-open"></i>
                    <p>Seu servidor conectou com sucesso, mas não encontramos nenhum Filme, Série ou Áudio no catálogo!</p>
                </div>`;
        }

        return true;

    } catch (error) {
        console.error("Failed to read Jellyfin assets:", error);
        return false;
    }
}

// Display elements dynamically
function renderMediaRows() {
    const libraryContainer = document.getElementById('mediaLibrary');
    libraryContainer.innerHTML = '';

    // Group items by Library Name or Type
    const categories = {};
    
    // Sort items by specific rows
    allMediaItems.forEach(item => {
        const laneName = item.libraryName || 'Gerais';
        if (!categories[laneName]) {
            categories[laneName] = [];
        }
        categories[laneName].push(item);
    });

    for (const laneName in categories) {
        const items = categories[laneName];
        if (items.length === 0) continue;

        // Apply filters if screen has a filter set
        const filtered = items.filter(it => {
            if (currentCategory === 'all') return true;
            if (currentCategory === 'Movie') return it.type === 'Movie' || (!it.isSeries && it.type !== 'Audio');
            if (currentCategory === 'Series') return it.isSeries || it.type === 'Series';
            if (currentCategory === 'Audio') return it.type === 'Audio';
            return true;
        });

        if (filtered.length === 0) continue;

        const laneElement = document.createElement('section');
        laneElement.className = 'media-lane';
        
        // Select appropriate icon
        let laneIcon = 'fa-clapperboard';
        if (laneName.toLowerCase().includes('músic') || laneName.toLowerCase().includes('áudio')) {
            laneIcon = 'fa-music';
        } else if (laneName.toLowerCase().includes('séri')) {
            laneIcon = 'fa-tv';
        }

        let gridCardsHtml = '';
        filtered.forEach(it => {
            gridCardsHtml += `
                <div class="media-card" onclick='openMediaDetail(${JSON.stringify(encodeURIComponent(JSON.stringify(it)))})'>
                    <img src="${it.posterUrl}" class="card-poster" alt="${it.title}" loading="lazy">
                    <span class="card-badge">${it.detailsSubtitle}</span>
                    <div class="card-info">
                        <div class="card-title">${it.title}</div>
                        <div class="card-meta">
                            <span>${it.year}</span>
                            <span>${it.runtime}</span>
                        </div>
                    </div>
                </div>
            `;
        });

        laneElement.innerHTML = `
            <div class="lane-header">
                <i class="fa-solid ${laneIcon}"></i>
                <h3>${laneName}</h3>
            </div>
            <div class="lane-grid">
                ${gridCardsHtml}
            </div>
        `;

        libraryContainer.appendChild(laneElement);
    }

    if (libraryContainer.innerHTML === '') {
        libraryContainer.innerHTML = `
            <div class="loading-state">
                <i class="fa-solid fa-filter"></i>
                <p>Nenhuma mídia encontrada com o filtro selecionado.</p>
            </div>`;
    }
}

// Switch categories filters
window.switchCategory = function(category) {
    currentCategory = category;
    
    // Manage CSS active states
    const navs = document.querySelectorAll('.nav-links a');
    navs.forEach(nav => {
        nav.classList.remove('active');
        if (nav.innerText === 'Início' && category === 'all') nav.classList.add('active');
        if (nav.innerText === 'Filmes' && category === 'Movie') nav.classList.add('active');
        if (nav.innerText === 'Séries' && category === 'Series') nav.classList.add('active');
        if (nav.innerText === 'Música' && category === 'Audio') nav.classList.add('active');
    });

    renderMediaRows();
};

function setHeroBanner(item) {
    if (!item) return;
    heroMediaItem = item;
    
    document.getElementById('heroTitle').innerText = item.title;
    document.getElementById('heroDesc').innerText = item.synopsis.substring(0, 180) + (item.synopsis.length > 180 ? '...' : '');
    
    const banner = document.getElementById('heroBanner');
    banner.style.backgroundImage = `linear-gradient(rgba(0,0,0,0.1), rgba(8, 8, 12, 0.95)), url('${item.backdropUrl}')`;
}

window.playCurrentHero = function() {
    if (heroMediaItem) {
        startStreamPlayer(heroMediaItem);
    }
};

window.showCurrentHeroDetails = function() {
    if (heroMediaItem) {
        showDetailsHTML(heroMediaItem);
    }
};

// Details screen popup loader
window.openMediaDetail = function(encodedItem) {
    const decoded = JSON.parse(decodeURIComponent(encodedItem));
    showDetailsHTML(decoded);
};

function showDetailsHTML(item) {
    currentSelectedMedia = item;

    document.getElementById('detailTitleText').innerText = item.title;
    document.getElementById('detailYear').innerHTML = `<i class="fa-regular fa-calendar"></i> ${item.year}`;
    document.getElementById('detailDuration').innerHTML = `<i class="fa-regular fa-clock"></i> ${item.runtime}`;
    document.getElementById('detailType').innerText = item.detailsSubtitle || "Mídia";
    document.getElementById('detailRating').innerText = item.rating;
    document.getElementById('detailSynopsisText').innerText = item.synopsis;

    document.getElementById('detailDirector').innerText = item.director || "N/A";
    document.getElementById('detailWriters').innerText = item.writers || "N/A";
    document.getElementById('detailGenres').innerText = item.genres || "N/A";

    // Extra dynamic details
    const musicContainer = document.getElementById('musicAlbumContainer');
    if (item.type === 'Audio') {
        musicContainer.style.display = 'block';
        document.getElementById('detailAlbum').innerText = item.album || "Gflixnet Wave";
    } else {
        musicContainer.style.display = 'none';
    }

    // Modal background image
    document.getElementById('detailBackdrop').style.backgroundImage = `url('${item.backdropUrl}')`;

    // Connect Play button
    document.getElementById('detailPlayBtn').onclick = () => {
        closeModal('detailModal');
        startStreamPlayer(item);
    };

    // Open detailed popover
    document.getElementById('detailModal').classList.add('active');
}

// Media Playbacks
function startStreamPlayer(item) {
    if (item.type === 'Audio') {
        // Player is an audio overlay
        playAudioDirectly(item);
    } else {
        // Player is a cinematic overlay
        openVideoOverlay(item);
    }
}

// Video overlay controls
function openVideoOverlay(item) {
    const playerModal = document.getElementById('playerModal');
    const video = document.getElementById('mediaVideoPlayer');
    const playerLoader = document.getElementById('playerLoader');

    document.getElementById('playerFooterTitle').innerText = item.title;
    document.getElementById('playerFooterSubtitle').innerText = `${item.detailsSubtitle} • ${item.genres}`;

    playerLoader.classList.remove('hide');
    playerModal.classList.add('active');

    video.src = item.streamUrl;
    
    video.oncanplay = () => {
        playerLoader.classList.add('hide');
    };

    video.onerror = () => {
        playerLoader.classList.add('hide');
        alert("Não foi possível carregar a transmissão direta. Verifique suas conexões locais.");
        closePlayerModal();
    };

    video.play();
}

window.closePlayerModal = function() {
    const video = document.getElementById('mediaVideoPlayer');
    video.pause();
    video.src = '';
    closeModal('playerModal');
};

// Music/Audio Playbacks
function playAudioDirectly(item) {
    const audioBar = document.getElementById('audioPlayerBar');
    const audioInstance = document.getElementById('audioInstance');
    const playIcon = document.getElementById('audioPlayIcon');
    const disc = document.getElementById('audioDisc');

    document.getElementById('audioTitle').innerText = item.title;
    document.getElementById('audioArtist').innerText = item.director || "Retro Beats";

    audioBar.style.display = 'flex';
    audioInstance.src = item.streamUrl;
    
    audioInstance.onplay = () => {
        playIcon.className = 'fa-solid fa-pause';
        disc.classList.add('playing');
    };

    audioInstance.onpause = () => {
        playIcon.className = 'fa-solid fa-play';
        disc.classList.remove('playing');
    };

    audioInstance.play();
}

window.toggleAudio = function() {
    const audioInstance = document.getElementById('audioInstance');
    if (audioInstance.paused) {
        audioInstance.play();
    } else {
        audioInstance.pause();
    }
};

window.closeAudioPlayer = function() {
    const audioInstance = document.getElementById('audioInstance');
    audioInstance.pause();
    audioInstance.src = '';
    document.getElementById('audioPlayerBar').style.display = 'none';
};
