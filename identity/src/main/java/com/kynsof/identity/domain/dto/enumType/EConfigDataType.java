package com.kynsof.identity.domain.dto.enumType;

/**
 * Tipos de datos soportados para los valores de configuración.
 *
 * Permite al cliente conocer cómo interpretar y convertir el valor string
 * almacenado en la base de datos al tipo de dato apropiado.
 */
public enum EConfigDataType {
    /**
     * Cadena de texto simple.
     * Ejemplo: "light", "https://example.com/logo.png"
     */
    STRING,

    /**
     * Número entero o decimal.
     * Ejemplo: "30", "99.99"
     */
    NUMBER,

    /**
     * Valor booleano.
     * Ejemplo: "true", "false"
     */
    BOOLEAN,

    /**
     * Objeto o array JSON serializado.
     * Ejemplo: "{\"key\":\"value\"}", "[1,2,3]"
     */
    JSON,

    /**
     * Valor encriptado (típicamente API keys, tokens, passwords).
     * El valor debe ser desencriptado antes de usarse.
     * Ejemplo: "AES256:abc123def456..."
     */
    ENCRYPTED,

    /**
     * URL válida.
     * Ejemplo: "https://api.whatsapp.com/v1"
     */
    URL,

    /**
     * Color en formato hexadecimal.
     * Ejemplo: "#1976D2", "#FF5722"
     */
    COLOR,

    /**
     * Fecha en formato ISO-8601.
     * Ejemplo: "2025-01-15T10:30:00Z"
     */
    DATE
}
