# 🚀 Instalação do Gflixnet no ZimaOS / CasaOS

Este guia rápido explica como instalar o **Gflixnet Web** e o canal de sideload do seu aplicativo Android diretamente no seu **ZimaOS** ou qualquer servidor rodando **CasaOS**.

---

## 🛠️ Método 1: Instalação Manual Super Rápida (Recomendado)

Como o ZimaOS suporta nativamente a importação direta de arquivos `docker-compose.yml`, você pode adicionar o Gflixnet com sua identidade visual completa em menos de 1 minuto!

### Passo a Passo:

1. Acesse o painel inicial do seu **ZimaOS**.
2. No canto superior direito da Loja de Aplicativos (App Store), clique no botão azul **"Adicionar um aplicativo conteinerizado"** (como visto no seu print-screen).
3. Na janela que se abrir, clique no ícone de **"Importar"** (geralmente representado por um ícone de pasta ou documento/YAML no topo superior direito da janela de configuração).
4. Copie e cole o código completo do painel abaixo dentro da caixa de texto:

```yaml
version: '3.8'

services:
  gflixnet-web:
    container_name: gflixnet-web
    image: gflixnet:latest
    restart: unless-stopped
    ports:
      - "8096:80"
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

5. Clique em **"Salvar"** ou **"Confirmar"**.
6. O ZimaOS preencherá automaticamente todos os parâmetros para você (Portas, Ícone, Título e Descrição).
7. Clique em **"Instalar"** (Install). Pronto!

---

## 📦 Método 2: Como colocar o Gflixnet em uma Loja de Aplicativos Personalizada (App Store)

Se você deseja disponibilizar o Gflixnet para ser instalado com um único clique de forma nativa por você e por outros usuários dentro da própria Loja Oficial ou através de uma loja de terceiros (App Store do ZimaOS/CasaOS), o procedimento padrão é:

### 1. Publicar a Imagem Docker
Primeiro, a imagem criada pelo seu `Dockerfile` precisa ser enviada para um registro público (como o [Docker Hub](https://hub.docker.com/)). Em seu terminal de desenvolvimento (ZimaOS ou computador local):
```bash
# Faça o build localmente
docker build -t seu-usuario-docker/gflixnet-web:latest .

# Envie para o Docker Hub
docker push seu-usuario-docker/gflixnet-web:latest
```

### 2. Configurar o Repositório da Loja do CasaOS/ZimaOS
As lojas de aplicativos do CasaOS e ZimaOS são simplesmente repositórios Git públicos que hospedam arquivos `docker-compose.yml`. Para listar seu app:

1. Faça um Fork do repositório oficial da App Store ou use um repositório de loja personalizado (como o [WisdomSky/CasaOS-AppStore](https://github.com/WisdomSky/CasaOS-AppStore)):
   * Repositório Oficial: `https://github.com/IceWhaleTech/CasaOS-AppStore`
2. Crie uma nova pasta dentro de `Apps/` chamada `Gflixnet/`.
3. Adicione o arquivo `docker-compose.yml` que editamos nesta pasta.
4. Faça um Pull Request para o repositório principal da loja ou crie sua própria Custom Store URL (basta adicionar a URL do seu repositório Git de aplicativos no botão `Adicionar Fonte` da loja do ZimaOS).

---

## ⭐️ Vantagens da Integração Nativa no ZimaOS:

* **Ícone e Interface Personalizados**: O aplicativo aparece no painel do ZimaOS com ícone específico e descrição detalhada.
* **Auto-Mapeamento de Portas**: A porta `8096` (Web) é detectada e vinculada ao ícone de clique automático do sistema.
* **Acesso do APK Estático**: Através do endereço IP do seu ZimaOS (por exemplo: `http://IP-DO-SEU-ZIMAOS:8096`), qualquer dispositivo móvel ou TV conectada à rede local poderá acessar a página web e realizar o download do APK `Gflixnet.apk` instantaneamente para instalação rápida.
