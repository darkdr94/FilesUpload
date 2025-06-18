# 📂 FilesUpload API

Servicio backend para la carga **multipart de archivos** hacia AWS S3. Implementado en Java 20 con Spring Boot 3.2.5, soporta URLs prefirmadas, validaciones y finalización segura de cargas por partes.

---

## 📑 Tabla de Contenido

1. [Instalacion](#instalacion)
2. [Variables de Entorno](#variables-de-entorno)
3. [Uso](#uso)
4. [Documentacion de la API](#documentacion-de-la-api)
5. [Tecnologias Usadas](#tecnologias-usadas)
6. [Contribuir](#contribuir)
7. [Licencia](#licencia)

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
```

---

## 💡 Uso

1. **Iniciar Multipart Upload**
   `POST /files-upload/generate-multipart-urls`
   Envía JSON con `{ filename, fileSizeBytes, contentType }`.
   Recibirás un objeto con `key`, `uploadId` y un array de URLs prefirmadas.

2. **Subir Partes a S3**
   `PUT {presigned_url}`
   Cada petición debe:

   * Usar método **PUT** a la URL prefirmada.
   * Incluir **raw bytes** de esa parte en el cuerpo.
   * Capturar el header `ETag` de la respuesta.

   > **Nota:** Estas solicitudes no pasan por tu backend.

3. **Completar Multipart Upload**
   `POST /files-upload/complete-multiparts-upload`
   Envía JSON con `key`, `uploadId` y `parts: [{ partNumber, eTag }, ...]` para que S3 ensamble el archivo.

---

## ☁️ S3 Multipart & JWT

| Propiedad                         | Descripción                                                                           |
| --------------------------------- | ------------------------------------------------------------------------------------- |
| `app.s3.presign-duration-minutes` | Duración **minutos** de validez de cada URL prefirmada. (Ej: `60` → expira en 1 hora) |
| `app.s3.part-size-megabytes`      | Tamaño **MB** de cada parte al generar las URLs. (Ej: `100` → partes de 100 MB)       |
| `security.jwt.expiration-ms`      | TTL **ms** del token JWT. (Ej: `3600000` → 3 600 000 ms = 1 hora)                     |

```properties
# S3 Multipart
aapp.s3.presign-duration-minutes=60
app.s3.part-size-megabytes=100

# JWT
security.jwt.expiration-ms=3600000
```

> ⚠️ Ajusta estos valores según rendimiento y seguridad:
>
> * URLs muy cortas → renuevos frecuentes.
> * Partes muy grandes → consumo de memoria.
> * JWT corto → re-login frecuente.

---

## 📘 Documentación de la API

La documentación interactiva está disponible en GitHub Pages:
🔗 [https://darkdr94.github.io/FilesUpload](https://darkdr94.github.io/FilesUpload)

---

## 🛠️ Tecnologías Usadas

* 🧐 Java 20
* 🔥 Spring Boot 3.2.5
* ☁️ AWS S3 (Multipart Upload)
* 🛡️ AWS SSM (Parameter Store)
* 📄 PostgreSQL
* 📜 Swagger / OpenAPI

---

## 🤝 Contribuir

1. Haz **fork** del repositorio.
2. Crea una **rama feature**.
3. Realiza tus cambios y haz **commit**.
4. Abre un **Pull Request**.

---

## 📍 Licencia

Este proyecto está bajo **MIT License** — ver `LICENSE` para detalles.
