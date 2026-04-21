package com.kynsof.identity.domain.dto.enumType;

public enum ELeadStatus {
    NEW,           // Recién capturado
    CONTACTED,     // Se ha contactado al lead
    QUALIFIED,     // Lead calificado como potencial cliente
    NEGOTIATING,   // En proceso de negociación
    CONVERTED,     // Convertido a Business + User
    DISCARDED      // Descartado (no interesado, datos falsos, etc.)
}