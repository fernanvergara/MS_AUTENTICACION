# Usa una imagen base de Java, por ejemplo, OpenJDK 17
FROM openjdk:21-jdk-slim

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el JAR ejecutable de la carpeta de construcción de Gradle
COPY applications/app-service/build/libs/autenticacion.jar autenticacion.jar

# Expone el puerto del servicio de autenticación
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "autenticacion.jar"]