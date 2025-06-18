# 📂 FilesUpload API

Servicio backend para la carga **multipart de archivos** hacia AWS S3. Implementado en Java 20 con Spring Boot 3.2.5, soporta URLs prefirmadas, validaciones y finalización segura de cargas por partes.

---

## 📑 Tabla de Contenido

1. [Prerequisitos](#-prerequisitos) 
2. [Instalación](#-instalación)
3. [Variables de Entorno](#%EF%B8%8F-variables-de-entorno)
4. [Uso](#-uso)
5. [Documentación de la API](#-documentación-de-la-api)
6. [Tecnologías Usadas](#%EF%B8%8F-tecnolog%C3%ADas-usadas)
7. [Contribuir](#-contribuir)
8. [Licencia](#-licencia)

---

## ✅ Prerequisitos

Antes de ejecutar este proyecto, se debe contar con lo siguiente:

### 🧾 Cuenta de AWS
Se debe tener una cuenta de AWS con acceso programático (Access Key + Secret Key) y:

- Un **usuario IAM** con permisos sobre:
  - **Amazon S3** (crear y listar buckets, y operaciones multipart)
  - **SSM Parameter Store** (lectura de parámetros seguros)
  - **CloudWatch Logs** (opcional, para monitoreo)
  - **RDS o PostgreSQL** (acceso a la base de datos)

### 🗄️ Base de Datos PostgreSQL
El backend requiere conexión a una base de datos PostgreSQL. Se puede:  

- Usar Amazon RDS u otra instancia accesible desde la app.
- Se debe tener:
  - URL de conexión (jdbc:postgresql://...)
  - Usuario y contraseña
  - Seguridad de red configurada para permitir acceso desde donde corra el backend
   - Crear la tabla principal:

 ```bash
  CREATE TABLE uploaded_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    filename TEXT NOT NULL,
    content_type TEXT,
    s3_key TEXT NOT NULL,
    upload_id TEXT,
    bucket_name TEXT,
    size_bytes BIGINT,
    uploaded_by TEXT,
    status TEXT DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT now()
);
```

> 📌 La base de datos puede estar en cualquier proveedor o en local, mientras sea accesible por red desde el backend.

## 🚀 Instalación

Se debe clonar el repositorio, configurar las variables de entorno y levantar la aplicación:

```bash
$ git clone https://github.com/darkdr94/FilesUpload.git
$ cd FilesUpload
$ ./mvnw spring-boot:run
```

---

## ⚙️ Variables de Entorno

### 🔒 Parámetros en AWS SSM

Estos parámetros **deben** existir en AWS Systems Manager Parameter Store y se cargan al iniciar la aplicación. En desarrollo se puede usar `application.properties`, **pero no** subir estos valores.

| Parámetro                   | Descripción                                            |
| --------------------------- | ------------------------------------------------------ |
| `app.ssm.db-url-param`      | SSM: URL de conexión a la base de datos PostgreSQL     |
| `app.ssm.db-username-param` | SSM: Nombre de usuario de la base de datos             |
| `app.ssm.db-password-param` | SSM: Contraseña del usuario de la base de datos        |
| `app.ssm.bucket-name-param` | SSM: Nombre raíz del bucket de S3                      |
| `app.ssm.user-password`     | SSM: Contraseña para el usuario de autenticación local |


### ☁️ S3 Multipart & JWT

Las siguientes variables corresponden a configuración de rendimiento y seguridad.

| Propiedad                         | Descripción                                                                           |
| --------------------------------- | ------------------------------------------------------------------------------------- |
| app.s3.presign-duration-minutes | Duración **minutos** de validez de cada URL prefirmada. (Ej: 60 → expira en 1 hora) |
| app.s3.part-size-megabytes      | Tamaño **MB** de cada parte al generar las URLs. (Ej: 100 → partes de 100 MB)       |
| security.jwt.expiration-ms      | TTL **ms** del token JWT. (Ej: 3600000 → 3 600 000 ms = 1 hora)                     |

> ⚠️ Ajustar estos valores según rendimiento y seguridad:
>
> * URLs prefirmadas con tiempos muy cortos → renuevos frecuentes.
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
   User: userdrv94 (sección de gestión de usuarios por construir) y password el que se haya definido en la variable `app.ssm.user-password`.  
   Este servicio genera el token JWT que debe enviarse en las otras peticiones en el Header Authorization como Bearer token.

### Flujo de carga multipart

1. **Inicia una carga multipart**  
   `POST /files-upload/generate-multipart-urls`  
   Envía los metadatos del archivo (nombre, tamaño, tipo) y recibe un conjunto de URLs prefirmadas para cargar las partes directamente a S3.

Para dividir un archivo de manera local en Windows se debe abrir la consola PowerShell y ejecutar el siguiente comnado:

   ```
    $partSize = 100MB
    $inputFile = "Ruta completa del archivo incluyendo extension" 
    $outputFolder = "Ruta de la carpeta donde quedaran las partes"
    New-Item -ItemType Directory -Force -Path $outputFolder | Out-Null
    
    $buffer = New-Object byte[] $partSize
    $index = 0
    $stream = [System.IO.File]::OpenRead($inputFile)
    
    while (($read = $stream.Read($buffer, 0, $partSize)) -gt 0) {
        $fileName = Join-Path $outputFolder ("part_{0:D3}.bin" -f $index)
        [System.IO.File]::WriteAllBytes($fileName, $buffer[0..($read-1)])
        $index++
    }
    $stream.Close()

   ```
Las partes del archivo quedaran con los nombres: part_000.bin, part_001.bin, ... etc

2. **Sube las partes directamente a S3**  
   `PUT {presigned_url}`  
   Desde el cliente (por ejemplo, navegador o frontend), se debe realizar una solicitud HTTP `PUT` a cada URL prefirmada recibida en el paso anterior.  
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

## 🤝 Contribuciones

Se agradece cualquier tipo de contribución que permita mejorar este proyecto.

Para contribuir, se recomienda:

1. Realizar un fork del repositorio.
2. Crear una rama para cada cambio propuesto.
3. Aplicar buenas prácticas de desarrollo, incluyendo documentación y pruebas si aplica.
4. Abrir un Pull Request describiendo claramente los cambios realizados y su propósito.

También es posible colaborar reportando errores o sugiriendo mejoras a través del sistema de Issues.

---

## 📄 Licencia

Este proyecto se encuentra disponible bajo los términos de la licencia [MIT](./LICENSE).

Esto permite utilizar, copiar, modificar, fusionar, publicar, distribuir y sublicenciar el software, respetando siempre el aviso de derechos de autor incluido en el repositorio.

Para mayor información, consultar el archivo [`LICENSE`](./LICENSE).

