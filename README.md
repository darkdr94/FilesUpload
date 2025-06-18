# 📂 FilesUpload API

Servicio backend para la carga **multipart de archivos** hacia AWS S3. Implementado en Java 20 con Spring Boot 3.2.5, soporta URLs prefirmadas, validaciones y finalización segura de cargas por partes.

---

## 📑 Tabla de Contenido

1. [Instalación](#-instalación)
2. [Variables de Entorno](#%EF%B8%8F-variables-de-entorno)
3. [Uso](#-uso)
4. [Documentación de la API](#-documentación-de-la-api)
5. [Tecnologías Usadas](#%EF%B8%8F-tecnolog%C3%ADas-usadas)
6. [Contribuir](#-contribuir)
7. [Licencia](#-licencia)

---

## 🚀 Instalación

Clona el repositorio y levanta la aplicación:

```bash
$ git clone https://github.com/darkdr94/FilesUpload.git
$ cd FilesUpload
$ ./mvnw spring-boot:run
```

---

## ⚙️ Variables de Entorno

### 🔒 Parámetros en AWS SSM

Estos parámetros **deben** existir en AWS Systems Manager Parameter Store y se cargan al iniciar la aplicación. En desarrollo puedes usar `application.properties`, **pero no** subir estos valores.

| Parámetro                   | Descripción                                            |
| --------------------------- | ------------------------------------------------------ |
| `app.ssm.db-url-param`      | SSM: URL de conexión a la base de datos PostgreSQL     |
| `app.ssm.db-username-param` | SSM: Nombre de usuario de la base de datos             |
| `app.ssm.db-password-param` | SSM: Contraseña del usuario de la base de datos        |
| `app.ssm.bucket-name-param` | SSM: Nombre raíz del bucket de S3                      |
| `app.ssm.user-password`     | SSM: Contraseña para el usuario de autenticación local |


### ☁️ S3 Multipart & JWT

Las siguientes variables corresponden a configuración de rendimiento y seguridad

| Propiedad                         | Descripción                                                                           |
| --------------------------------- | ------------------------------------------------------------------------------------- |
| app.s3.presign-duration-minutes | Duración **minutos** de validez de cada URL prefirmada. (Ej: 60 → expira en 1 hora) |
| app.s3.part-size-megabytes      | Tamaño **MB** de cada parte al generar las URLs. (Ej: 100 → partes de 100 MB)       |
| security.jwt.expiration-ms      | TTL **ms** del token JWT. (Ej: 3600000 → 3 600 000 ms = 1 hora)                     |

> ⚠️ Ajusta estos valores según rendimiento y seguridad:
>
> * URLs cont tiempos muy cortos → renuevos frecuentes.
> * Partes muy grandes → consumo de memoria.
> * JWT corto → re-login frecuente.


### 📋 `application.properties` (ejemplo)

```properties
spring.application.name=filestorage
server.port=8080
server.address=0.0.0.0

spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
spring.http.encoding.force=true

logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg%n
logging.level.root=INFO
logging.level.com.drv.filestorage=DEBUG

# Parámetros AWS SSM (reemplazar por tus rutas)
app.ssm.db-url-param=
app.ssm.db-username-param=
app.ssm.db-password-param=
app.ssm.bucket-name-param=
app.ssm.user-password=

#S3 Multipart & JWT
app.s3.presign-duration-minutes=60
app.s3.part-size-megabytes=100
security.jwt.expiration-ms=3600000
```

---

## 💡 Uso

### Autenticación
1. `POST /auth/login`  
   User: userdrv94 (sección de gestión de usuarios por construir) y password el que hayas definido en la variable `app.ssm.user-password`  
   Genera el token JWT que debe enviarse en el Header Authorization como Bearer token

### Flujo de carga multipart

1. **Inicia una carga multipart**  
   `POST /files-upload/generate-multipart-urls`  
   Envía los metadatos del archivo (nombre, tamaño, tipo) y recibe un conjunto de URLs prefirmadas para cargar las partes directamente a S3.

2. **Sube las partes directamente a S3**  
   `PUT {presigned_url}`  
   Desde el cliente (por ejemplo, navegador o frontend), realiza una solicitud HTTP `PUT` a cada URL prefirmada recibida en el paso anterior.  
   Cada solicitud debe incluir una parte del archivo **en formato binario** (raw bytes) en el cuerpo de la petición.  
   > **Importante**: Estas cargas se hacen directamente a S3, sin pasar por el backend.

3. **Finaliza la carga**  
   `POST /files-upload/complete-multiparts-upload`  
   Envía la lista de partes cargadas (con sus `ETags` y `partNumber`) para que S3 ensamble el archivo final.

---

## 📘 Documentación de la API

🔗 La documentación interactiva está disponible en GitHub Pages: [![Ver Swagger UI](https://img.shields.io/badge/Swagger-UI-green)](https://darkdr94.github.io/FilesUpload/)

---

## 🛠️ Tecnologías Usadas

* 🧠 Java 20
* 🔥 Spring Boot 3.2.5
* ☁️ AWS S3 (Multipart Upload)
* 🛡️ AWS SSM (Parameter Store)
* 📄 PostgreSQL
* 📜 Swagger / OpenAPI

---

## 🤝 Contribuir

¡Las contribuciones son bienvenidas!
Puedes crear un Pull Request o reportar un Issue para colaborar con mejoras o nuevas funcionalidades.

---

## 📍 Licencia

Este proyecto está licenciado bajo la licencia MIT.
Puedes reutilizarlo libremente incluyendo el aviso de copyright original.
