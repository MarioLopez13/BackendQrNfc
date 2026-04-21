package com.kynsof.identity.domain.interfaces.service;

import com.kynsof.identity.domain.dto.mailjet.SendMailJetEmailRequestDto;
import com.kynsof.identity.domain.dto.mailjet.SendMailjetEmailMessageDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Interfaz para interactuar con el servicio CloudBridges para la gestión de archivos
 */
public interface ICloudBridgesFileService {

    /**
     * Sube un archivo al servicio CloudBridges
     * @param file Archivo a subir
     * @param objectId ID del objeto asociado al archivo (opcional)
     * @param folderPath Ruta de la carpeta donde se guardará el archivo (opcional)
     * @param userId ID del usuario que sube el archivo (opcional)
     * @param userName Nombre del usuario que sube el archivo (opcional)
     * @param businessId ID del negocio asociado (opcional)
     * @return Respuesta del servicio con la información del archivo subido
     */
    ResponseEntity<?> uploadFile(
            MultipartFile file,
            String objectId,
            String folderPath,
            String userId,
            String userName,
            UUID businessId);

    /**
     * Sube un archivo al servicio CloudBridges con parámetros simplificados
     * @param file Archivo a subir
     * @param objectId ID del objeto asociado al archivo
     * @param folderPath Ruta de la carpeta donde se guardará el archivo
     * @return Respuesta del servicio con la información del archivo subido
     */
    ResponseEntity<?> uploadFile(
            MultipartFile file,
            String objectId,
            String folderPath);
            
    /**
     * Envía un correo electrónico a través del servicio CloudBridges utilizando Mailjet
     * @param emailRequest Objeto que contiene toda la información necesaria para enviar el correo
     * @return Respuesta del servicio con el resultado del envío
     */
    ResponseEntity<SendMailjetEmailMessageDto> sendEmail(SendMailJetEmailRequestDto emailRequest);
}
