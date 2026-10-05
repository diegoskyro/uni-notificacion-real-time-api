# Etapa 1: Runtime base usando Java 21 (Temurin)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Configurar zona horaria (America/Bogota)
RUN apk add --no-tzdata tzdata && \
    cp /usr/share/zoneinfo/America/Bogota /etc/localtime && \
    echo "America/Bogota" > /etc/timezone && \
    apk del tzdata

# Copiar el JAR generado previamente por el Maven Package en Jenkins
COPY target/*.jar app.jar

# Exponer el puerto 8082 del microservicio
EXPOSE 8082

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=dev
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Comando de entrada
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
