package com.kynsof.identity.domain.dto.enumType;

/**
 * Categorías de configuración para agrupar y organizar las configuraciones de negocio.
 *
 * Permite filtrar y buscar configuraciones por su propósito funcional,
 * facilitando la gestión de configuraciones relacionadas.
 */
public enum EConfigCategory {
    /**
     * Configuraciones relacionadas con la identidad visual y branding.
     * Ejemplos: colores primarios/secundarios, logos, temas, fuentes, favicon
     */
    BRANDING,

    /**
     * Integraciones con servicios externos de terceros.
     * Ejemplos: WhatsApp API, Firebase, Zoom, Odoo, Stripe, Twilio
     */
    INTEGRATIONS,

    /**
     * Configuraciones de seguridad y autenticación.
     * Ejemplos: timeout de sesión, 2FA, expiración de contraseñas, políticas de password
     */
    SECURITY,

    /**
     * Configuraciones de notificaciones y alertas.
     * Ejemplos: habilitar email/SMS/push, plantillas de mensajes, frecuencia
     */
    NOTIFICATIONS,

    /**
     * Configuraciones de localización e internacionalización.
     * Ejemplos: idioma, zona horaria, formato de fecha/hora, moneda
     */
    LOCALIZATION,

    /**
     * Límites y cuotas del negocio.
     * Ejemplos: máximo de usuarios, almacenamiento, citas por día, transacciones
     */
    LIMITS,

    /**
     * Configuraciones relacionadas con pagos y facturación.
     * Ejemplos: métodos de pago habilitados, comisiones, impuestos, descuentos
     */
    BILLING,

    /**
     * Configuraciones de funcionalidades específicas de la aplicación.
     * Ejemplos: módulos habilitados, features flags, permisos por defecto
     */
    FEATURES,

    /**
     * Configuraciones generales que no encajan en otras categorías.
     */
    GENERAL
}
