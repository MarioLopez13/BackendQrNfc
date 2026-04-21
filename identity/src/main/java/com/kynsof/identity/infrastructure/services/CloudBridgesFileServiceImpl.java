package com.kynsof.identity.infrastructure.services;

import com.kynsof.identity.domain.dto.mailjet.SendMailJetEmailRequestDto;
import com.kynsof.identity.domain.dto.mailjet.SendMailjetEmailMessageDto;
import com.kynsof.identity.domain.interfaces.service.ICloudBridgesFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Implementación del servicio para interactuar con el servicio CloudBridges
 */
@Service
public class CloudBridgesFileServiceImpl implements ICloudBridgesFileService {

    private static final Logger logger = LoggerFactory.getLogger(CloudBridgesFileServiceImpl.class);
    
    private final RestTemplate restTemplate;
    private final String cloudBridgesFileUploadUrl;
    private final String cloudBridgesEmailUrl;

    @Autowired
    public CloudBridgesFileServiceImpl(
            RestTemplate restTemplate, 
            @Qualifier("cloudBridgesFileUploadUrl") String cloudBridgesFileUploadUrl,
            @Qualifier("cloudBridgesEmailUrl") String cloudBridgesEmailUrl) {
        this.restTemplate = restTemplate;
        this.cloudBridgesFileUploadUrl = cloudBridgesFileUploadUrl;
        this.cloudBridgesEmailUrl = cloudBridgesEmailUrl;
    }

    @Override
    public ResponseEntity<?> uploadFile(
            MultipartFile file,
            String objectId,
            String folderPath,
            String userId,
            String userName,
            UUID businessId) {
        
        logger.info("Iniciando carga de archivo en CloudBridges: {}, folderPath: {}, objectId: {}", 
                file.getOriginalFilename(), folderPath, objectId);

        try {
            // Preparar los headers con información del usuario
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            if (userId != null && !userId.isEmpty()) {
                headers.set("X-User-Id", userId);
            }
            
            if (userName != null && !userName.isEmpty()) {
                headers.set("X-User-Name", userName);
            }

            // Preparar el cuerpo de la solicitud
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new HttpEntity<>(file.getResource()));
            
            if (objectId != null && !objectId.isEmpty()) {
                body.add("objectId", objectId);
            }
            
            if (folderPath != null && !folderPath.isEmpty()) {
                body.add("folderPath", folderPath);
            }
            
            if (businessId != null) {
                body.add("businessId", businessId.toString());
            }

            // Preparar la entidad HTTP
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Realizar la solicitud POST
            ResponseEntity<?> response = restTemplate.exchange(
                    cloudBridgesFileUploadUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Object.class
            );

            logger.info("Archivo cargado exitosamente en CloudBridges: {}, estado: {}", 
                    file.getOriginalFilename(), response.getStatusCode());
            
            return response;
        } catch (Exception e) {
            logger.error("Error al subir el archivo a CloudBridges: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al subir el archivo: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> uploadFile(MultipartFile file, String objectId, String folderPath) {
        // Llamar al método completo con valores nulos para los parámetros opcionales
        return uploadFile(file, objectId, folderPath, null, null, null);
    }
    
    @Override
    public ResponseEntity<SendMailjetEmailMessageDto> sendEmail(SendMailJetEmailRequestDto emailRequest) {
        logger.info("Enviando correo electrónico a través de CloudBridges: asunto: {}, template: {}", 
                emailRequest.getSubject(), emailRequest.getTemplateId());
        
        try {
            // Preparar los headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Preparar la entidad HTTP
            HttpEntity<SendMailJetEmailRequestDto> requestEntity = new HttpEntity<>(emailRequest, headers);
            
            // Realizar la solicitud POST
            ResponseEntity<SendMailjetEmailMessageDto> response = restTemplate.exchange(
                    cloudBridgesEmailUrl,
                    HttpMethod.POST,
                    requestEntity,
                    SendMailjetEmailMessageDto.class
            );
            
            logger.info("Correo electrónico enviado exitosamente a través de CloudBridges, estado: {}", 
                    response.getStatusCode());
            
            return response;
        } catch (Exception e) {
            logger.error("Error al enviar el correo electrónico a través de CloudBridges: {}", e.getMessage(), e);
            SendMailjetEmailMessageDto errorResponse = new SendMailjetEmailMessageDto(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
}
