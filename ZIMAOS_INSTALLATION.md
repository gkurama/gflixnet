# 🚀 Instalação do Gflixnet no ZimaOS / CasaOS

Este guia explica como instalar o **Gflixnet Web & APK Portal** diretamente no seu **ZimaOS** ou qualquer servidor rodando **CasaOS**, resolvendo de forma definitiva o erro *"Falha ao baixar imagem: repositório não existe"*.

---

## 🔍 Por que o erro *"Repositório não existe"* aconteceu?

O erro ocorreu porque o Docker Compose anterior tentava usar uma imagem local chamada `gflixnet:latest`. Como o ZimaOS busca as imagens direto no Docker Hub e ela não havia sido enviada para a nuvem pública, o sistema acusava que ela não existia.

Para resolver isso de forma **profissional, segura e sem erros**, agora configuramos o aplicativo para usar a imagem oficial do **Nginx** (super leve e segura, baixada direto da biblioteca oficial do Docker) e ler os arquivos modificados do Gflixnet e o APK diretamente do armazenamento do seu ZimaOS!

---

## 🛠️ Método Recomendado: Instalação por Mapeamento de Pasta (100% livre de erros)

Este método leva menos de 2 minutos e garante que o ícone do seu aplicativo, os estilos visuais, o player do navegador e o APK estejam sempre integrados perfeitamente.

### 📋 Passo 1: Preparar a Pasta no ZimaOS

1. Abra a interface do seu **ZimaOS** e vá no aplicativo oficial **Files** (Arquivos / Disco).
2. Vá para o diretório de dados dos aplicativos, geralmente localizado em:
   `Arquivos locais` ➔ `AppData`
3. Crie uma nova pasta chamada `gflixnet` exatamente no seguinte endereço:
   `/DATA/AppData/gflixnet`
4. Dentro desta nova pasta `gflixnet`, copie ou envie os 5 arquivos de frontend do Gflixnet (você pode baixá-los do editor do AI Studio em um zip ou individualmente):
   * `index.html` (o arquivo HTML da interface)
   * `style.css` (os estilos elegantes e escuros)
   * `app.js` (a lógica do portal)
   * `manifest.json` (as configurações do app para iOS/Android)
   * `Gflixnet.apk` (o APK compilado para Android/TV que fica dentro do seu painel do AI Studio em `/web/Gflixnet.apk`)

---

### 📝 Passo 2: Adicionar o Aplicativo pelo Yaml do ZimaOS

1. Com os arquivos já carregados na pasta do seu ZimaOS, volte ao painel principal do sistema.
2. Na Loja de Aplicativos (App Store), clique no botão azul localizado no canto superior direito: **"Adicionar um aplicativo conteinerizado"** (ou "Adicionar aplicativo manual").
3. Na janela que abrir, clique no pequeno botão de **"Importar"** (representado por uma folha com lápis ou ícone de YAML, localizado no canto superior direito da janela de configuração).
4. Copie e cole todo o código YAML atualizado abaixo dentro da caixa de texto:

```yaml
version: '3.8'

services:
  gflixnet-web:
    container_name: gflixnet-web
    image: nginx:alpine
    restart: unless-stopped
    ports:
      - "8096:80"
    volumes:
      - /DATA/AppData/gflixnet:/usr/share/nginx/html
    environment:
      - TZ=America/Sao_Paulo

x-casaos:
  architectures:
    - amd64
    - arm64
  main: gflixnet-web
  author: "Gflixnet Team"
  category: "Video"
  icon: "https://raw.githubusercontent.com/jellyfin/jellyfin-ux/master/branding/svg/jellyfin-icon-transparent.svg"
  index: /
  port_map: "8096"
  scheme: http
  developer: "Gflixnet"
  title:
    en_us: "Gflixnet Web"
    pt_br: "Gflixnet Web & APK Portal"
  tagline:
    en_us: "Cast and play your cinematic content directly from ZimaOS"
    pt_br: "Seu cinema pessoal e sideload do app para Android no ZimaOS"
  description:
    en_us: "Access your Gflixnet and play back media using a premium cinematic interface. It also lets you download and sideload the mobile/TV Android APK directly onto your devices."
    pt_br: "Acesse sua biblioteca do Gflixnet através do navegador com interface de cinema. Permite baixar e fazer o sideload do APK Android compilado em seus dispositivos móveis ou Smart TVs."
```

5. Clique em **"Confirmar"** ou **"Salvar"** (o ZimaOS preencherá todo o painel automaticamente com o logotipo do Jellyfin, nome correto, categorias, rotas e portas).
6. Depois clique em **"Instalar"** (Install). O ZimaOS irá baixar a imagem do `nginx:alpine` em segundos e inicializará seu portal.

---

## ⭐️ Vantagens deslumbrantes do novo formato no seu Painel:

* **Instantâneo**: Como usa o Nginx oficial, instala sem compilação demorada em qualquer processador (ARM64 como o ZimaBoard Lite ou AMD64 clássico Intel).
* **Porta de Cinema Dedicada**: O sistema se conectará automaticamente através da porta de mídia corporativa `8096` vinculando-se perfeitamente aos seus serviços de rede.
* **Instalação em Smart TVs e Celulares**: Qualquer smartphone ou TV conectada na mesma rede local que abrir o endereço `http://IP-DO-SEU-ZIMAOS:8096` poderá clicar em **"Baixar para Android"** para obter o APK de forma 100% nativa em segundos!
* **Instalação no iPhone (iOS)**: Ao acessar o navegador Safari em um iPhone, toque no botão de compartilhar e selecione "Adicionar à Tela de Início" para ter o app nativo no iOS.

