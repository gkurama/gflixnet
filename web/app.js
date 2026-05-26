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
let heroInterval = null;
let currentHeroIndex = 0;

// Mock / Demo Media Data
const demoMediaData = [
    {
        id: "bib_1",
        title: "Paulo, o Apóstolo",
        year: 2025,
        runtime: "1h 48min",
        isSeries: false,
        isBiblical: true,
        detailsSubtitle: "Filme Bíblico",
        rating: "12+",
        genres: "Bíblico, Drama, História",
        tags: "Bíblico, Fé, 1080p",
        synopsis: "Paulo, que passa de perseguidor infame de cristãos ao apóstolo mais influente de Jesus Cristo, vive seus últimos dias em uma cela fria em Roma, aguardando a execução pelo imperador Nero. Lucas, seu amigo e médico romano, o visita para registrar sua fascinante história.",
        director: "Andrew Hyatt",
        writers: "Andrew Hyatt",
        language: "Português",
        awards: "Destaque Fé",
        libraryName: "Filmes Bíblicos",
        posterUrl: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=400",
        backdropUrl: "https://images.unsplash.com/photo-1507679799987-c73779587ccf?q=80&w=1200",
        streamUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    },
    {
        id: "bib_2",
        title: "The Chosen: Os Escolhidos",
        year: 2024,
        runtime: "Série",
        isSeries: true,
        isBiblical: true,
        detailsSubtitle: "Série Bíblica",
        rating: "Livre",
        genres: "Bíblico, Drama, Histórico",
        tags: "Bíblico, Série, Popular",
        synopsis: "Um pescador carismático que se afoga em dívidas, uma mulher perturbada enfrentando seus próprios demônios e um jovem cobrador de impostos ostracizado pela sociedade encontram um homem misterioso de Nazaré que transforma suas vidas para sempre.",
        director: "Dallas Jenkins",
        writers: "Dallas Jenkins",
        language: "Português",
        awards: "Série do Ano",
        libraryName: "Séries Bíblicas",
        posterUrl: "https://images.unsplash.com/photo-1510137600163-2729bc6959a6?q=80&w=400",
        backdropUrl: "https://images.unsplash.com/photo-1510137600163-2729bc6959a6?q=80&w=1200",
        streamUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
    },
    {
        id: "bib_3",
        title: "A Paixão de Cristo",
        year: 2024,
        runtime: "2h 06min",
        isSeries: false,
        isBiblical: true,
        detailsSubtitle: "Filme Bíblico",
        rating: "14+",
        genres: "Bíblico, Drama, Épico",
        tags: "Bíblico, Clássico, HDR",
        synopsis: "Uma representação profunda e visceral das últimas doze horas da vida de Jesus de Nazaré em Jerusalém, retratando o dia da sua crucificação com extraordinária carga emocional e fidelidade bíblica.",
        director: "Mel Gibson",
        writers: "Benedict Fitzgerald",
        language: "Português",
        awards: "Campeão de Bilheteria",
        libraryName: "Filmes Bíblicos",
        posterUrl: "https://images.unsplash.com/photo-1509024644558-2f56ce76c490?q=80&w=400",
        backdropUrl: "https://images.unsplash.com/photo-1509024644558-2f56ce76c490?q=80&w=1200",
        streamUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
    },
    {
        id: "bib_4",
        title: "Reis",
        year: 2024,
        runtime: "Série",
        isSeries: true,
        isBiblical: true,
        detailsSubtitle: "Série Bíblica",
        rating: "12+",
        genres: "Bíblico, Drama, Épico",
        tags: "Bíblico, Produção Nacional",
        synopsis: "A fantástica saga da monarquia em Israel, desde a transição de juízes até a unificação dos reinos sob a liderança de Saul, as batalhas heróicas do Rei Davi e os ensinamentos majestosos do sábio Salomão.",
        director: "Leonardo Miranda",
        writers: "Raphaela Castro",
        language: "Português",
        awards: "Sucesso Audiovisual de Fé",
        libraryName: "Séries Bíblicas",
        posterUrl: "https://images.unsplash.com/photo-1473163928189-364b2c4e1135?q=80&w=400",
        backdropUrl: "https://images.unsplash.com/photo-1473163928189-364b2c4e1135?q=80&w=1200",
        streamUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    },
    {
        id: "bib_5",
        title: "Os Dez Mandamentos",
        year: 2023,
        runtime: "2h 10min",
        isSeries: false,
        isBiblical: true,
        detailsSubtitle: "Filme Bíblico",
        rating: "Livre",
        genres: "Bíblico, Épico, Drama",
        tags: "Bíblico, Clássico, História",
        synopsis: "A inspiradora jornada do profeta Moisés desde o seu nascimento em uma cesta flutuante no Egito até a liderança do povo rumo à libertação, cruzando o Mar Vermelho e recebendo as tábuas da Lei no topo do Sinai.",
        director: "Alexandre Avancini",
        writers: "Vivian de Oliveira",
        language: "Português",
        awards: "Recorde de Bilheteria",
        libraryName: "Filmes Bíblicos",
        posterUrl: "https://images.unsplash.com/photo-1519817650390-64a93db51149?q=80&w=400",
        backdropUrl: "https://images.unsplash.com/photo-1519817650390-64a93db51149?q=80&w=1200",
        streamUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
    },
    {
        id: "demo_1",
        title: "Tojima Wants to Be a Kamen Rider",
        year: 2024,
        runtime: "Série",
        isSeries: true,
        detailsSubtitle: "Série de Tokusatsu",
        rating: "10+",
        genres: "Ação, Fantasia, Tokusatsu",
        tags: "Jellyfin, 1080p, Legendado",
        synopsis: "Tojima é um jovem nerd apaixonado pela cultura de super-heróis japoneses. Ao encontrar um cinto de transformação misterioso abandonado, ele depara-se com uma conspiração cibernética sombria e decide assumir o manto do lendário Kamen Rider.",
        director: "Shinichiro Shirakura",
        writers: "Shotaro Ishinomori",
        language: "Japonês",
        awards: "Destaque Tokusatsu",
        libraryName: "Séries de TV",
        posterUrl: "https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=400",
        backdropUrl: "https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=1200",
        streamUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    },
    {
        id: "demo_2",
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
        id: "demo_3",
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
        id: "demo_4",
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
        id: "demo_5",
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
        streamUrl: "https://commondatastorage.googleapis.com/codeskulptor-demos/DinoandRose/rose_song.mp3"
    }
];

// Instantly check if an item belongs to biblical category
function isBiblicalItem(item) {
    if (!item) return false;
    return item.isBiblical === true;
}

// Document Ready Initialization
document.addEventListener('DOMContentLoaded', () => {
    initApp();
    setupSplashTimer();
});

function setupSplashTimer() {
    const splash = document.getElementById('apkSplashScreen');
    if (!splash) return;
    
    // Simulate premium APK loading
    setTimeout(() => {
        splash.style.opacity = '0';
        setTimeout(() => {
            splash.style.display = 'none';
        }, 500);
    }, 2800);
}

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
        }, 3200); // Trigger just after splash fades
    }
}

// Global modal handling
window.openAuthModal = function() {
    document.getElementById('authModal').classList.add('active');
};

window.openIosInstallModal = function() {
    document.getElementById('iosInstallModal').classList.add('active');
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
    
    // Choose first as hero and start automatic carousel rotation
    setHeroBanner(allMediaItems[0]);
    startHeroCarousel();
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
        
        // Exibe um estado elegante de falha de conexão na biblioteca вместо do spinner infinito!
        document.getElementById('mediaLibrary').innerHTML = `
            <div class="loading-state connection-error-state" style="padding: 40px 20px; text-align: center; color: white; display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 300px; gap: 15px;">
                <i class="fa-solid fa-triangle-exclamation" style="font-size: 54px; color: #FF9800; margin-bottom: 5px;"></i>
                <h3 style="font-size: 20px; font-weight: 700;">Sem Conexão com o Servidor</h3>
                <p style="color: var(--text-muted); max-width: 450px; line-height: 1.5; font-size: 14px;">Não foi possível autenticar ou conectar ao servidor Jellyfin em <span style="color: var(--primary-cyan); font-weight: 600;">${currentServerUrl}</span>.</p>
                <p style="font-size: 12px; color: rgba(255,255,255,0.4); max-width: 400px; margin-top: -5px;">Por favor, confirme se o seu servidor local está online, se a URL está correta e se o CORS está devidamente habilitado.</p>
                <div style="display: flex; gap: 15px; justify-content: center; margin-top: 15px;">
                    <button class="btn btn-primary" onclick="window.openAuthModal()" style="display: flex; align-items: center; gap: 8px;"><i class="fa-solid fa-server"></i> Configurar Servidor</button>
                    <button class="btn btn-secondary" onclick="window.useDemoMode()" style="display: flex; align-items: center; gap: 8px; background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255,255,255,0.1);"><i class="fa-solid fa-circle-play"></i> Modo de Demonstração</button>
                </div>
            </div>
        `;
    }
}

async function fetchAndRenderJellyfinLibrary() {
    const indicator = document.getElementById('statusIndicator');
    const statusText = document.getElementById('statusText');
    const authHeader = `MediaBrowser Client="Gflixnet", Device="WebBrowser", DeviceId="gflixnet_web_client", Version="1.0.0", Token="${currentToken}"`;

    try {
        // Fetch items recursive
        const response = await fetch(`${currentServerUrl}/Users/${currentUserId}/Items?IncludeItemTypes=Movie,Series,Audio&Recursive=true&Fields=PrimaryImageAspectRatio,BasicSyncInfo,Overview,Genres,ProductionYear,RunTimeTicks,OfficialRating,Studios,Path`, {
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
            let isSeries = item.Type === "Series";
            const isAudio = item.Type === "Audio";
            const pathStr = item.Path || "";

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
                ? `${currentServerUrl}/Audio/${item.Id}/stream?static=true&api_key=${currentToken}`
                : `${currentServerUrl}/Videos/${item.Id}/stream?static=true&api_key=${currentToken}`;

            // Determine if the item path or context classifies this as Biblical or TV Series
            const normPath = pathStr.toLowerCase();
            const normName = (item.Name || "").toLowerCase();
            const normGenres = item.Genres ? item.Genres.join(", ").toLowerCase() : "";
            const normOverview = (item.Overview || "").toLowerCase();

            let isBiblicalFinal = false;

            // Check if this matches any Biblical keywords anywhere in the item metadata
            const holdsBiblicalKeywords = 
                normPath.includes("biblicos") || normPath.includes("biblico") || normPath.includes("bíb") || normPath.includes("biblica") || 
                normPath.includes("chosen") || normPath.includes("jesus") ||
                normName.includes("bíbl") || normName.includes("bibl") || normName.includes("chosen") || normName.includes("jesus") || 
                normName.includes("cristo") || normName.includes("moisés") || normName.includes("moises") || normName.includes("rei davi") || 
                normName.includes("salomão") || normName.includes("gênesis") || normName.includes("genesis") || normName.includes("apocalipse") || 
                normName.includes("paulo,") || normName.includes(" escolhidos") || 
                normGenres.includes("bíb") || normGenres.includes("bibl") ||
                normOverview.includes("bíblica") || normOverview.includes("bíblico") || normOverview.includes("fidelidade bíblica") || normOverview.includes("fidelidade biblica");

            if (holdsBiblicalKeywords) {
                isBiblicalFinal = true;
            }

            let libraryNameFinal = isAudio ? "Músicas" : (isSeries ? "Séries de TV" : "Filmes recomendados");

            if (isBiblicalFinal) {
                if (isSeries) {
                    libraryNameFinal = "Séries Bíblicas";
                } else {
                    libraryNameFinal = "Filmes Bíblicos";
                }
            } else if (normPath.includes("/series") || normPath.includes("media/series") || normPath.includes("/séries") || normPath.includes("media/séries")) {
                isSeries = true;
                libraryNameFinal = "Séries de TV";
            } else if (normPath.includes("/filmes") || normPath.includes("/filme")) {
                isSeries = false;
                libraryNameFinal = "Filmes recomendados";
            } else if (normPath.includes("/musica") || normPath.includes("/música") || normPath.includes("/musicas") || normPath.includes("/músicas") || isAudio) {
                isSeries = false;
                libraryNameFinal = "Músicas";
            } else {
                if (isAudio) {
                    libraryNameFinal = "Músicas";
                } else if (isSeries) {
                    libraryNameFinal = "Séries de TV";
                } else {
                    libraryNameFinal = "Filmes recomendados";
                }
            }

            const detailSubStr = isAudio ? "Música" : (isBiblicalFinal ? (isSeries ? "Série Bíblica" : "Filme Bíblico") : (isSeries ? "Série de TV" : "Filme"));

            return {
                id: item.Id,
                title: item.Name,
                year: item.ProductionYear || 2024,
                runtime: runtimeString,
                isSeries: isSeries,
                isBiblical: isBiblicalFinal,
                type: item.Type,
                detailsSubtitle: detailSubStr,
                rating: item.OfficialRating || "Livre",
                genres: item.Genres ? item.Genres.join(", ") : "Geral",
                tags: isAudio ? "Jellyfin Audio" : "Jellyfin 1080p",
                synopsis: item.Overview || "Mídia hospedada no seu servidor Jellyfin.",
                director: item.Artists ? item.Artists.join(", ") : "Hospedagem Externa",
                writers: "Jellyfin Core",
                libraryName: libraryNameFinal,
                posterUrl: posterUrl,
                backdropUrl: backdropUrl,
                streamUrl: streamUrl,
                path: pathStr
            };
        });

        // Set indicator to online
        indicator.className = 'status-circle online';
        statusText.innerText = 'Online';

        if (allMediaItems.length > 0) {
            renderMediaRows();
            setHeroBanner(allMediaItems[0]);
            startHeroCarousel();
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

// Display elements dynamically in horizontal rows or APK vertical grids
function renderMediaRows() {
    const libraryContainer = document.getElementById('mediaLibrary');
    if (!libraryContainer) return;
    
    libraryContainer.innerHTML = '';
    
    // Automatically smooth-scroll to page body start on category switches for premium UX
    window.scrollTo({ top: 0, behavior: 'smooth' });

    if (currentCategory === 'all') {
        // Horizontal cinematic lanes grouped by type/thematic folders
        const lanes = {
            "Filmes recentes": allMediaItems.filter(it => !it.isSeries && it.type !== 'Audio' && !isBiblicalItem(it)),
            "Séries recentes": allMediaItems.filter(it => it.isSeries && !isBiblicalItem(it)),
            "Filmes Bíblicos recentes": allMediaItems.filter(it => !it.isSeries && it.type !== 'Audio' && isBiblicalItem(it)),
            "Séries Bíblicas recentes": allMediaItems.filter(it => it.isSeries && isBiblicalItem(it)),
            "Músicas recentes": allMediaItems.filter(it => it.type === 'Audio')
        };

        for (const laneName in lanes) {
            const list = lanes[laneName];
            if (list.length === 0) continue;

            const laneElement = document.createElement('section');
            laneElement.className = 'media-lane';
            
            let laneIcon = 'fa-clapperboard';
            if (laneName.includes('Músic')) laneIcon = 'fa-music';
            else if (laneName.includes('Séri')) laneIcon = 'fa-tv';
            else if (laneName.includes('Bíb')) laneIcon = 'fa-book-bible';

            let targetCat = 'all';
            if (laneName.includes('Filmes Bíblicos')) targetCat = 'biblical_movies';
            else if (laneName.includes('Séries Bíblicas')) targetCat = 'biblical_series';
            else if (laneName.includes('Filmes')) targetCat = 'Movie';
            else if (laneName.includes('Séries')) targetCat = 'Series';
            else if (laneName.includes('Músic')) targetCat = 'Audio';

            let gridCardsHtml = '';
            list.forEach(it => {
                gridCardsHtml += `
                    <div class="media-card" onclick='openMediaDetail("${encodeURIComponent(JSON.stringify(it))}")'>
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
                <div class="lane-header" onclick="switchCategory('${targetCat}')">
                    <div class="lane-header-left">
                        <i class="fa-solid ${laneIcon}"></i>
                        <h3>${laneName} <span class="lane-chevron-symbol">&gt;</span></h3>
                    </div>
                </div>
                <div class="lane-grid">
                    ${gridCardsHtml}
                </div>
            `;
            libraryContainer.appendChild(laneElement);
        }
    } else {
        // Render a high-fidelity vertical grid (matching the mobile APK catalogs)
        let filtered = [];
        let categoryTitle = "";
        
        if (currentCategory === 'Movie') {
            filtered = allMediaItems.filter(it => !it.isSeries && it.type !== 'Audio' && !isBiblicalItem(it));
            categoryTitle = "Filmes";
        } else if (currentCategory === 'Series') {
            filtered = allMediaItems.filter(it => it.isSeries && !isBiblicalItem(it));
            categoryTitle = "Séries de TV";
        } else if (currentCategory === 'Audio') {
            filtered = allMediaItems.filter(it => it.type === 'Audio');
            categoryTitle = "Músicas";
        } else if (currentCategory === 'biblical_movies') {
            filtered = allMediaItems.filter(it => !it.isSeries && it.type !== 'Audio' && isBiblicalItem(it));
            categoryTitle = "Filmes Bíblicos";
        } else if (currentCategory === 'biblical_series') {
            filtered = allMediaItems.filter(it => it.isSeries && isBiblicalItem(it));
            categoryTitle = "Séries Bíblicas";
        }

        let gridCardsHtml = '';
        filtered.forEach(it => {
            gridCardsHtml += `
                <div class="media-card grid-style" onclick='openMediaDetail("${encodeURIComponent(JSON.stringify(it))}")'>
                    <div class="poster-container">
                        <img src="${it.posterUrl}" class="card-poster" alt="${it.title}" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=400'">
                        <span class="card-badge">${it.detailsSubtitle}</span>
                    </div>
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

        const gridLayoutHtml = `
            <div class="apk-category-layout">
                <!-- Red back action banner matching visual APK patterns exactly -->
                <div class="apk-grid-back-banner" onclick="switchCategory('all')">
                    <div class="back-circle-red"><i class="fa-solid fa-arrow-left"></i></div>
                    <div class="back-texts">
                        <div class="back-title">Voltar ao Início</div>
                        <div class="back-desc">Toque para retornar à página principal</div>
                    </div>
                </div>
                
                <div class="apk-grid-header-row">
                    <div class="category-info-pills">
                        <h2 class="apk-category-heading">${categoryTitle}</h2>
                        <span class="apk-count-badge">1-${filtered.length} de ${filtered.length}</span>
                    </div>
                    <div class="apk-grid-controls">
                        <i class="fa-solid fa-shuffle" onclick="playRandomFromGrid('${encodeURIComponent(JSON.stringify(filtered))}')" title="Reproduzir Aleatório" style="cursor: pointer; padding: 6px; color: var(--primary-cyan);"></i>
                        <i class="fa-solid fa-border-all active" style="color: var(--primary-cyan);"></i>
                        <i class="fa-solid fa-arrow-down-a-z" style="color: var(--text-muted);"></i>
                    </div>
                </div>

                <div class="apk-vertical-grid">
                    ${gridCardsHtml || `<div class="empty-category-notice" style="grid-column: 1/-1; padding: 40px; text-align: center; color: var(--text-muted);"><i class="fa-solid fa-folder-closed" style="font-size: 32px; margin-bottom: 10px; display: block;"></i>Sua biblioteca local do Jellyfin não sincronizou arquivos nesta pasta bíblica específica. Para ver o conteúdo de demonstração, ative o Modo Demonstração no header.</div>`}
                </div>
            </div>
        `;
        
        libraryContainer.innerHTML = gridLayoutHtml;
    }
}

// Play random media item from within the current grid context
window.playRandomFromGrid = function(encodedList) {
    try {
        const list = JSON.parse(decodeURIComponent(encodedList));
        if (list && list.length > 0) {
            const rand = list[Math.floor(Math.random() * list.length)];
            startStreamPlayer(rand);
        }
    } catch (e) {
        console.error("Rand play from grid failed:", e);
    }
};

// Periodic loop for showcase carousel rotation
function startHeroCarousel() {
    if (heroInterval) clearInterval(heroInterval);
    currentHeroIndex = 0;
    
    // Choose movies or series to cycle as featured banners
    const showable = allMediaItems.filter(it => it.type === 'Movie' || it.isSeries || it.type === 'Series');
    if (showable.length === 0) return;
    
    heroInterval = setInterval(() => {
        currentHeroIndex = (currentHeroIndex + 1) % showable.length;
        setHeroBanner(showable[currentHeroIndex]);
    }, 5000); // Rotates every 5 seconds passed in loop
}

// Switch categories filters
window.switchCategory = function(category) {
    currentCategory = category;
    
    // Manage CSS active states in header
    const navs = document.querySelectorAll('.nav-links a');
    navs.forEach(nav => {
        nav.classList.remove('active');
        if (nav.innerText.trim() === 'Início' && category === 'all') nav.classList.add('active');
        if (nav.innerText.trim() === 'Filmes' && category === 'Movie') nav.classList.add('active');
        if (nav.innerText.trim() === 'Séries' && category === 'Series') nav.classList.add('active');
        if (nav.innerText.trim() === 'Música' && category === 'Audio') nav.classList.add('active');
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

// Dynamic APK detail and Episode/Season lists layout mirroring
let currentEpisodes = [];

async function fetchEpisodesForSeries(seriesId) {
    if (isDemoMode) {
        // Mock episodes styled matching Tojima Wants to Be a Kamen Rider S01
        return [
            { id: "demo_ep1", name: "O Despertar de Tojima", episodeNumber: 1, seasonNumber: 1, duration: "23m", thumbnailUrl: "https://upload.wikimedia.org/wikipedia/commons/c/c5/Big_Buck_Bunny_Screen_01.png", streamUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" },
            { id: "demo_ep2", name: "Treinamento Intensivo", episodeNumber: 2, seasonNumber: 1, duration: "24m", thumbnailUrl: "https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=600", streamUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4" },
            { id: "demo_ep3", name: "A Chegada de Babi", episodeNumber: 3, seasonNumber: 1, duration: "23m", thumbnailUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/8/8f/Sintel_poster.jpg/800px-Sintel_poster.jpg", streamUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4" },
            { id: "demo_ep4", name: "Ameaça Cibernética", episodeNumber: 4, seasonNumber: 1, duration: "22m", thumbnailUrl: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=600", streamUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" },
            { id: "demo_ep5", name: "Kamen Rider para Sempre", episodeNumber: 5, seasonNumber: 1, duration: "26m", thumbnailUrl: "https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=1200", streamUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4" }
        ];
    }

    const authHeader = `MediaBrowser Client="Gflixnet", Device="WebBrowser", DeviceId="gflixnet_web_client", Version="1.0.0", Token="${currentToken}"`;
    try {
        const response = await fetch(`${currentServerUrl}/Shows/${seriesId}/Episodes?userId=${currentUserId}`, {
            headers: {
                'X-Emby-Authorization': authHeader
            }
        });
        if (!response.ok) throw new Error("Could not load episodes");
        const data = await response.json();
        return data.Items.map(ep => {
            let durationStr = "N/A";
            if (ep.RunTimeTicks) {
                const totalSeconds = Math.floor(ep.RunTimeTicks / 10000000);
                const minutes = Math.floor(totalSeconds / 60);
                durationStr = `${minutes}m`;
            }
            const thumbUrl = ep.ImageTags && ep.ImageTags.Primary
                ? `${currentServerUrl}/Items/${ep.Id}/Images/Primary`
                : (currentSelectedMedia ? currentSelectedMedia.backdropUrl : 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=600');
            
            return {
                id: ep.Id,
                name: ep.Name || `Episódio ${ep.IndexNumber || ''}`,
                episodeNumber: ep.IndexNumber || 1,
                seasonNumber: ep.ParentIndexNumber || 1,
                duration: durationStr,
                thumbnailUrl: thumbUrl,
                streamUrl: `${currentServerUrl}/Videos/${ep.Id}/stream?static=true&api_key=${currentToken}`
            };
        });
    } catch (e) {
        console.error("Error fetching Jellyfin episodes:", e);
        return [];
    }
}

function renderMainDetailView(item, episodesList = []) {
    const isSeries = item.isSeries === true;
    
    let playActionJs = isSeries 
        ? `window.playEpisodeDirectlyFromList(0)` 
        : `window.playMainMedia()`;

    let html = `
        <button class="apk-back-btn" onclick="closeModal('detailModal')"><i class="fa-solid fa-arrow-left"></i></button>
        <div class="apk-banner" style="background-image: url('${item.backdropUrl}')">
            <div class="apk-banner-overlay"></div>
            <div class="apk-banner-play-overlay" onclick="${playActionJs}">
                <i class="fa-solid fa-play"></i>
            </div>
        </div>
        <div class="apk-body">
            <h2 class="apk-title">${item.title}</h2>
            <div class="apk-meta">
                <span class="apk-meta-type">${item.detailsSubtitle}</span>
                <span>&bull;</span>
                <span>${item.year}</span>
                <span>&bull;</span>
                <span class="rating-badge-green">${item.rating}</span>
            </div>
            
            <div class="apk-actions-row">
                <button class="apk-action-circle play" onclick="${playActionJs}"><i class="fa-solid fa-play"></i></button>
                <button class="apk-action-circle" onclick="${isSeries ? 'window.shuffleEpisodes()' : 'window.playMainMedia()'}"><i class="fa-solid fa-shuffle"></i></button>
                <button class="apk-action-circle"><i class="fa-solid fa-check"></i></button>
                <button class="apk-action-circle" onclick="window.toggleFavoriteSelected()"><i class="fa-regular fa-heart" id="apkFavoriteIcon"></i></button>
                <button class="apk-action-circle"><i class="fa-solid fa-ellipsis-vertical"></i></button>
            </div>
            
            <div class="apk-tags-row">
                <strong>Etiquetas:</strong> <span>${item.genres}, Jellyfin, Streaming</span>
            </div>
            
            <p class="apk-synopsis">${item.synopsis}</p>
    `;

    if (isSeries) {
        if (episodesList.length === 0) {
            // Loading element
            html += `
                <div class="apk-section-title">Temporadas</div>
                <div style="text-align: center; padding: 20px; color: var(--text-muted);">
                    <i class="fa-solid fa-circle-notch fa-spin" style="font-size: 24px; color: var(--primary-cyan); margin-bottom: 10px;"></i>
                    <p style="font-size: 13px;">Sincronizando temporadas e episódios do Jellyfin...</p>
                </div>
            `;
        } else {
            // "A seguir" section
            const firstEp = episodesList[0];
            html += `
                <div class="apk-section-title">A seguir</div>
                <div class="apk-aseguir-card" onclick="window.playEpisodeDirectlyFromList(0)">
                    <div class="apk-aseguir-thumb-container">
                        <img src="${firstEp.thumbnailUrl}" />
                        <div class="apk-aseguir-play-overlay">
                            <i class="fa-solid fa-play"></i>
                        </div>
                    </div>
                    <div class="apk-aseguir-title">S${firstEp.seasonNumber}:E${firstEp.episodeNumber} - ${firstEp.name}</div>
                </div>
                
                <div class="apk-section-title">Temporadas</div>
                <div class="apk-seasons-row">
            `;
            
            // Group episodes by season
            const seasonsMap = {};
            episodesList.forEach(ep => {
                if (!seasonsMap[ep.seasonNumber]) {
                    seasonsMap[ep.seasonNumber] = [];
                }
                seasonsMap[ep.seasonNumber].push(ep);
            });
            
            const sortedSeasonNums = Object.keys(seasonsMap).map(Number).sort((a,b) => a - b);
            sortedSeasonNums.forEach(seasonNum => {
                const epsCount = seasonsMap[seasonNum].length;
                html += `
                    <div class="apk-season-card" onclick="window.openSeasonView(${seasonNum})">
                        <span class="num-indicator">${seasonNum}</span>
                        <span class="season-name">Temporada ${seasonNum}</span>
                        <span class="season-eps-count">${epsCount} episódios</span>
                    </div>
                `;
            });
            
            html += `</div>`;
        }
    }

    html += `</div>`; // Close apk-body
    document.getElementById('detailModalContent').innerHTML = html;
}

window.openSeasonView = function(seasonNumber) {
    if (!currentSelectedMedia) return;
    
    const seasonEps = currentEpisodes.filter(ep => ep.seasonNumber === seasonNumber);
    
    let html = `
        <button class="apk-back-btn cyan-btn" onclick="window.restoreMainDetailView()"><i class="fa-solid fa-arrow-left"></i></button>
        <div class="apk-body">
            <div class="apk-season-view-header">
                <span class="apk-season-series-title">${currentSelectedMedia.title}</span>
                <h2 class="apk-season-title">Temporada ${seasonNumber}</h2>
            </div>
            
            <div class="apk-actions-row">
                <button class="apk-action-circle play" onclick="window.playEpisodeFromSeason(${seasonNumber}, 0)"><i class="fa-solid fa-play"></i></button>
                <button class="apk-action-circle" onclick="window.shuffleSeasonEpisodes(${seasonNumber})"><i class="fa-solid fa-shuffle"></i></button>
            </div>
            
            <div class="apk-episodes-list">
    `;
    
    seasonEps.forEach((ep, idx) => {
        html += `
            <div class="apk-episode-item" onclick="window.playEpisodeFromSeason(${seasonNumber}, ${idx})">
                <div class="apk-episode-thumb">
                    <img src="${ep.thumbnailUrl}" />
                    <div class="play-overlay">
                        <i class="fa-solid fa-play"></i>
                    </div>
                </div>
                <div class="apk-episode-info">
                    <span class="apk-episode-name">${ep.episodeNumber}. ${ep.name}</span>
                    <span class="apk-episode-duration">${ep.duration}</span>
                </div>
                <i class="fa-solid fa-circle-info apk-episode-info-icon"></i>
            </div>
        `;
    });
    
    html += `
            </div>
        </div>
    `;
    
    document.getElementById('detailModalContent').innerHTML = html;
};

window.restoreMainDetailView = function() {
    if (currentSelectedMedia) {
        renderMainDetailView(currentSelectedMedia, currentEpisodes);
    }
};

window.playMainMedia = function() {
    if (currentSelectedMedia) {
        closeModal('detailModal');
        startStreamPlayer(currentSelectedMedia);
    }
};

window.playEpisodeDirectlyFromList = function(index) {
    if (currentEpisodes && currentEpisodes[index]) {
        closeModal('detailModal');
        const ep = currentEpisodes[index];
        startStreamPlayer({
            title: `S${ep.seasonNumber}:E${ep.episodeNumber} - ${ep.name}`,
            detailsSubtitle: `Temporada ${ep.seasonNumber} • Episódio ${ep.episodeNumber}`,
            genres: currentSelectedMedia ? currentSelectedMedia.genres : "Série de TV",
            streamUrl: ep.streamUrl,
            type: "Video"
        });
    }
};

window.playEpisodeFromSeason = function(seasonNumber, index) {
    const seasonEps = currentEpisodes.filter(ep => ep.seasonNumber === seasonNumber);
    const ep = seasonEps[index];
    if (ep) {
        closeModal('detailModal');
        startStreamPlayer({
            title: `S${ep.seasonNumber}:E${ep.episodeNumber} - ${ep.name}`,
            detailsSubtitle: `Temporada ${ep.seasonNumber} • Episódio ${ep.episodeNumber}`,
            genres: currentSelectedMedia ? currentSelectedMedia.genres : "Série de TV",
            streamUrl: ep.streamUrl,
            type: "Video"
        });
    }
};

window.shuffleEpisodes = function() {
    if (currentEpisodes.length > 0) {
        const randIndex = Math.floor(Math.random() * currentEpisodes.length);
        window.playEpisodeDirectlyFromList(randIndex);
    }
};

window.shuffleSeasonEpisodes = function(seasonNumber) {
    const seasonEps = currentEpisodes.filter(ep => ep.seasonNumber === seasonNumber);
    if (seasonEps.length > 0) {
        const randIndex = Math.floor(Math.random() * seasonEps.length);
        const ep = seasonEps[randIndex];
        const globalIndex = currentEpisodes.indexOf(ep);
        if (globalIndex !== -1) {
            window.playEpisodeDirectlyFromList(globalIndex);
        }
    }
};

window.toggleFavoriteSelected = function() {
    const icon = document.getElementById('apkFavoriteIcon');
    if (!icon) return;
    if (icon.classList.contains('fa-regular')) {
        icon.className = 'fa-solid fa-heart';
        icon.style.color = '#E50914';
    } else {
        icon.className = 'fa-regular fa-heart';
        icon.style.color = 'white';
    }
};

async function showDetailsHTML(item) {
    currentSelectedMedia = item;
    currentEpisodes = []; // Reset old chapters list

    // Render loading background and base metadata structure
    renderMainDetailView(item, []);

    // Open popover modal right away
    document.getElementById('detailModal').classList.add('active');

    if (item.isSeries === true) {
        // Fetch real episodes in parallel matching the APK layout
        const episodes = await fetchEpisodesForSeries(item.id);
        currentEpisodes = episodes;
        
        // Re-paint to render "A seguir" and Season pills
        renderMainDetailView(item, episodes);
    }
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

    // Remove any previous error fallback overlays
    const oldFallback = document.getElementById('playerErrorFallback');
    if (oldFallback) oldFallback.remove();

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
        
        // Render absolute direct stream fallback inside the video container
        const urlStr = item.streamUrl || "";
        const fallbackHtml = `
            <div id="playerErrorFallback" style="position: absolute; inset: 0; background: #050508; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 30px; text-align: center; color: white; z-index: 100; gap: 15px;">
                <i class="fa-solid fa-triangle-exclamation" style="font-size: 50px; color: var(--primary-cyan); text-shadow: 0 0 15px rgba(0, 210, 255, 0.45);"></i>
                <h3 style="font-size: 20px; font-weight: 800; color: white; letter-spacing: -0.5px;">Failsafe do Player Gflixnet</h3>
                <p style="color: var(--text-muted); max-width: 500px; font-size: 13.5px; line-height: 1.5; margin: 0 auto;">
                    O navegador bloqueou a reprodução direta. Isso acontece quando seu servidor Jellyfin (<span style="color: var(--primary-cyan);">${currentServerUrl}</span>) usa uma conexão insegura HTTP normal, e Gflixnet roda seguro em HTTPS (Bloqueio de Conteúdo Misto).
                </p>
                <p style="color: #FF9800; font-size: 12px; font-weight: 600; max-width: 450px; margin: 0 auto;">
                    <i class="fa-solid fa-circle-info"></i> Não se preocupe! Você pode abrir e assistir essa transmissão diretamente na sua rede local em 1 clique:
                </p>
                <div style="display: flex; gap: 12px; margin-top: 10px; justify-content: center;">
                    <a href="${urlStr}" target="_blank" class="btn btn-primary" onclick="window.closePlayerModal()" style="background: var(--primary-cyan); color: black; display: flex; align-items: center; gap: 8px; font-weight: 700; text-decoration: none; padding: 12px 24px; border-radius: 12px;">
                        <i class="fa-solid fa-up-right-from-square"></i> Abrir em Nova Guia
                    </a>
                    <button class="btn btn-secondary" onclick="window.closePlayerModal()" style="padding: 12px 24px; border-radius: 12px;">Fechar</button>
                </div>
            </div>
        `;
        
        const videoContainer = document.querySelector('.video-container');
        if (videoContainer) {
            const div = document.createElement('div');
            div.innerHTML = fallbackHtml;
            videoContainer.appendChild(div.firstElementChild);
        }
    };

    // Handle play promise correctly to prevent UI locks on browser safety blockages
    const playPromise = video.play();
    if (playPromise !== undefined) {
        playPromise.then(() => {
            console.log("Video started playing successfully.");
        }).catch(err => {
            console.warn("Autoplay blocked or required interaction. Clicking play explicitly helps.", err);
        });
    }
}

window.closePlayerModal = function() {
    const video = document.getElementById('mediaVideoPlayer');
    if (video) {
        video.pause();
        video.src = '';
    }
    
    // Cleanup any lingering error panels on close to reset layout
    const oldFallback = document.getElementById('playerErrorFallback');
    if (oldFallback) oldFallback.remove();
    
    closeModal('playerModal');
};

// Music/Audio Playbacks
let audioIsMuted = false;
let savedAudioVolume = 100;

function playAudioDirectly(item) {
    const audioBar = document.getElementById('audioPlayerBar');
    const audioInstance = document.getElementById('audioInstance');
    const playIcon = document.getElementById('audioPlayIcon');
    const disc = document.getElementById('audioDisc');
    const volumeSlider = document.getElementById('audioVolumeSlider');

    document.getElementById('audioTitle').innerText = item.title;
    document.getElementById('audioArtist').innerText = item.director || "Retro Beats";

    audioBar.style.display = 'flex';
    audioInstance.src = item.streamUrl;
    
    // Explicitly configure values
    audioInstance.muted = audioIsMuted;
    audioInstance.volume = savedAudioVolume / 100;
    if (volumeSlider) {
        volumeSlider.value = savedAudioVolume;
    }
    updateVolumeIcon(savedAudioVolume);

    audioInstance.onplay = () => {
        playIcon.className = 'fa-solid fa-pause';
        disc.classList.add('playing');
    };

    audioInstance.onpause = () => {
        playIcon.className = 'fa-solid fa-play';
        disc.classList.remove('playing');
    };

    audioInstance.onerror = (e) => {
        console.error("Audio stream playback failed:", e);
        alert("Não foi possível decodificar ou carregar esta faixa de áudio. Verifique se o servidor Jellyfin está respondendo normalmente ou se ela necessita de transcodificação de codec específico.");
    };

    // Modern browser autoplay prevention safe wrapper
    const audioPromise = audioInstance.play();
    if (audioPromise !== undefined) {
        audioPromise.then(() => {
            console.log("Audio playing unmuted successfully!");
        }).catch(err => {
            console.warn("Autoplay block detected on music stream. Falling back to paused state waiting for press.", err);
            playIcon.className = 'fa-solid fa-play';
            disc.classList.remove('playing');
        });
    }
}

window.toggleMuteAudio = function() {
    const audioInstance = document.getElementById('audioInstance');
    if (!audioInstance) return;

    audioIsMuted = !audioIsMuted;
    audioInstance.muted = audioIsMuted;

    const volumeSlider = document.getElementById('audioVolumeSlider');
    if (audioIsMuted) {
        if (volumeSlider) volumeSlider.value = 0;
        updateVolumeIcon(0);
    } else {
        if (volumeSlider) volumeSlider.value = savedAudioVolume;
        updateVolumeIcon(savedAudioVolume);
    }
};

window.changeAudioVolume = function(value) {
    const audioInstance = document.getElementById('audioInstance');
    if (!audioInstance) return;

    const vol = parseInt(value, 10);
    savedAudioVolume = vol;
    audioInstance.volume = vol / 100;

    if (vol === 0) {
        audioIsMuted = true;
        audioInstance.muted = true;
    } else {
        audioIsMuted = false;
        audioInstance.muted = false;
    }
    updateVolumeIcon(vol);
};

function updateVolumeIcon(vol) {
    const icon = document.getElementById('audioVolumeIcon');
    if (!icon) return;

    if (audioIsMuted || vol === 0) {
        icon.className = 'fa-solid fa-volume-xmark';
    } else if (vol < 40) {
        icon.className = 'fa-solid fa-volume-low';
    } else {
        icon.className = 'fa-solid fa-volume-high';
    }
}

window.toggleAudio = function() {
    const audioInstance = document.getElementById('audioInstance');
    if (audioInstance.paused) {
        const playPromise = audioInstance.play();
        if (playPromise !== undefined) {
            playPromise.catch(err => {
                console.error("Manual audio trigger failed:", err);
            });
        }
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
