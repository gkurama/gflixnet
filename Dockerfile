# Dockerfile - Gflixnet Web App & APK Server for ZimaOS
# Este Dockerfile serve o player web completo do Gflixnet e disponibiliza o download direto do APK Android pré-compilado.

# --- Estágio único e super leve baseado em Nginx ---
FROM nginx:alpine
LABEL maintainer="Gflixnet Team"
LABEL description="Centro de Mídia Gflixnet Web & APK Portal para ZimaOS"

# Diretório padrão para servir arquivos estáticos do Nginx
WORKDIR /usr/share/nginx/html

# Remove arquivos padrões do Nginx
RUN rm -rf ./*

# Copia a interface Web responsiva do Gflixnet
COPY web/index.html .
COPY web/style.css .
COPY web/app.js .
COPY web/manifest.json .

# Copia o APK Android já compilado de forma limpa pelo Gradle (assembleDebug)
# Isso torna a criação da imagem Docker extremamente leve e rápida no ZimaOS (sem precisar compilar o Android SDK na CPU do NAS)
COPY app/build/outputs/apk/debug/app-debug.apk ./Gflixnet.apk

# Expõe a porta padrão para a Web
EXPOSE 80

# Inicia o servidor Web Nginx
CMD ["nginx", "-g", "daemon off;"]
