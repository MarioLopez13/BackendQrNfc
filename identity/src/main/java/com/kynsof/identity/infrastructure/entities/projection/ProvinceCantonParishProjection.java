package com.kynsof.identity.infrastructure.entities.projection;

import java.util.UUID;

public interface ProvinceCantonParishProjection {

    // Datos de la Provincia
    UUID getProvinceId();
    String getProvinceName();
    String getProvinceType(); // "PROVINCE"

    // Datos del Cantón
    UUID getCantonId();
    String getCantonName();
    String getCantonType();   // "CANTON"

    // Datos de la Parroquia
    UUID getParishId();
    String getParishName();
    String getParishType();   // "PARROQUIA"
}