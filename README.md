# 📂 FilesUpload API

Servicio backend para la carga **multipart de archivos** hacia AWS S3. Implementado en Java 20 con Spring Boot 3.2.5, soporta URLs prefirmadas, validaciones y finalización segura de cargas por partes.

---

## 📁 Tabla de Contenido

* [Instalación](#instalación)
* [Uso](#uso)
* [Variables de Entorno](#variables-de-entorno)
* [Documentación de la API](#documentación-de-la-api)
* [Tecnologías Usadas](#tecnologías-usadas)
* [Contribuir](#contribuir)
* [Licencia](#licencia)

---

## 🚀 Instalación

Clona el proyecto y ejecuta localmente:

```bash
git clone https://github.com/darkdr94/FilesUpload.git
cd FilesUpload
./mvnw spring-boot:run
```

---

## ⚙️ Variables de Entorno y Parámetros configurados en AWS SSM

Estos parámetros deben estar definidos en AWS Systems Manager Parameter Store y son leídos por la aplicación en tiempo de ejecución. Puedes definirlos localmente en `application.properties` solo para desarrollo, **pero no deben ser publicados**.

| Parámetro                            | Descripción                                                                 |
|-------------------------------------|-----------------------------------------------------------------------------|
| `app.ssm.db-url-param`              | Ruta en SSM que contiene la URL de conexión a la base de datos PostgreSQL. |
| `app.ssm.db-username-param`         | Ruta en SSM que contiene el nombre de usuario de la base de datos.         |
| `app.ssm.db-password-param`         | Ruta en SSM que contiene la contraseña del usuario de la base de datos.    |
| `app.ssm.bucket-name-param`         | Ruta en SSM que contiene el nombre base del bucket de S3 utilizado.        |
| `app.ssm.user-password`             | Ruta en SSM con la contraseña cifrada o en texto plano para el usuario de autenticación local. |


```properties
# Configuración general
spring.application.name=filestorage
server.port=8080
server.address=0.0.0.0

# Base de datos
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Codificación
spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
spring.http.encoding.force=true

# Logging
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg%n
logging.level.root=INFO
logging.level.com.drv.filestorage=DEBUG

# Parámetros desde AWS SSM O puedes definirlos localmente pero NO publicarlos
app.ssm.db-url-param=
app.ssm.db-username-param=
app.ssm.db-password-param=
app.ssm.bucket-name-param=
app.ssm.user-password=/filestorage/auth/userdrv94-password

# Configuración del S3 multipart
app.s3.presign-duration-minutes=60
app.s3.part-size-megabytes=100

# JWT
security.jwt.expiration-ms=3600000
```

---

## 💡 Uso

1. **Inicia una carga multipart**  
   `POST /files-upload/generate-multipart-urls`  
   Envía los metadatos del archivo (nombre, tamaño, tipo) y recibe un conjunto de URLs prefirmadas para cargar las partes directamente a S3.

2. **Sube las partes directamente a S3**  
   `PUT {presigned_url}`  
   Desde el cliente (por ejemplo, navegador o frontend), realiza una solicitud HTTP `PUT` a cada URL prefirmada recibida en el paso anterior.  
   Cada solicitud debe incluir una parte del archivo **en formato binario** (raw bytes) en el cuerpo de la petición. Debes obtener el valor del header "eTag" de cada una de las peticiones.  
   > **Importante**: Estas cargas se hacen directamente a S3, sin pasar por el backend.


3. **Finaliza la carga**  
   `POST /files-upload/complete-multiparts-upload`  
   Envía la lista de partes cargadas (con sus `eTags` y `partNumber`) para que S3 ensamble el archivo final.

---

## 📘 Documentación de la API

Swagger UI está desplegado en GitHub Pages:

👉 [https://darkdr94.github.io/FilesUpload](https://darkdr94.github.io/FilesUpload)

---

## 🛠️ Tecnologías Usadas

* 🧐 Java 20
* 🔥 Spring Boot 3.2.5
* ☁️ AWS S3 (Multipart Upload)
* 🛡️ AWS SSM (Parameter Store)
* 📄 PostgreSQL
* 📜 Swagger / OpenAPI

---

## 🤝 Contribuir

¡Las contribuciones son bienvenidas!
Puedes crear un **Pull Request** o reportar un **Issue** para colaborar con mejoras o nuevas funcionalidades.

---

## 📍 Licencia

Este proyecto está licenciado bajo la licencia **MIT**.
Puedes reutilizarlo libremente incluyendo el aviso de copyright original.

---
