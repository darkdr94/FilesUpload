package com.drv.filestorage.common;

public class SwaggerExamples {

    public static final String EXAMPLE_SUCCESS_RESPONSE = """
            {
                "success": true,
                "message": "Multipart upload iniciado correctamente para el archivo test.png",
                "data": {
                    "key": "usuario/2025/06/uuid_test_test.png",
                    "uploadId": "EjemploUploadId",
                    "urls": [
                        { "partNumber": 1, "url": "https://s3...1" },
                        { "partNumber": 2, "url": "https://s3...2" }
                    ]
                }
            }
            """;

    public static final String EXAMPLE_VALIDATION_ERROR = """
            {
                "success": false,
                "data": null,
                "errors": {
                    "filename": "El nombre del archivo solo puede contener letras, números, guiones, puntos y guiones bajos"
                },
                "message": "Errores de validación"
            }
            """;

    public static final String EXAMPLE_UNAUTHORIZED = """
            {
                "status": 401,
                "error": "Unauthorized",
                "message": "El encabezado de autorizacion es invalido o no esta presente.",
                "path": "/files-upload/generate-multipart-urls"
            }
            """;

    public static final String EXAMPLE_INTERNAL_ERROR = """
            {
                "error": "INTERNAL_SERVER_ERROR",
                "message": "Ocurrio un error inesperado",
                "status": 500
            }
            """;

    public static final String EXAMPLE_COMPLETE_SUCCESS = """
            {
                "success": true,
                "data": null,
                "errors": null,
                "message": "Subida completada exitosamente"
            }
            """;

    public static final String EXAMPLE_COMPLETE_VALIDATION_ERROR = """
            {
                "success": false,
                "data": null,
                "errors": {
                    "key": "El nombre del archivo (key) no puede estar vacío"
                },
                "message": "Errores de validación"
            }
            """;
}
