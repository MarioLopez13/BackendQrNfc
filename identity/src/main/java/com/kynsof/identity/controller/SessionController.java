package com.kynsof.identity.controller;

import com.kynsof.identity.application.command.session.deleteAllSessions.DeleteAllSessionsCommand;
import com.kynsof.identity.application.command.session.deleteAllSessions.DeleteAllSessionsMessage;
import com.kynsof.identity.application.command.session.deleteSession.DeleteSessionCommand;
import com.kynsof.identity.application.command.session.deleteSession.DeleteSessionMessage;
import com.kynsof.identity.application.query.session.getActiveSessions.GetActiveSessionsQuery;
import com.kynsof.identity.application.query.session.getActiveSessions.GetActiveSessionsResponse;
import com.kynsof.share.core.domain.response.ApiResponse;
import com.kynsof.share.core.infrastructure.bus.IMediator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para gestionar sesiones activas de usuarios en Keycloak.
 *
 * Endpoints:
 * - GET /api/sessions/{userId} - Listar todas las sesiones activas de un usuario
 * - DELETE /api/sessions/{userId}/{sessionId} - Eliminar una sesión específica
 * - DELETE /api/sessions/{userId}/all - Eliminar todas las sesiones de un usuario
 */
@RestController
@RequestMapping("/api/sessions")
@Slf4j
public class SessionController {

    private final IMediator mediator;

    public SessionController(IMediator mediator) {
        this.mediator = mediator;
    }

    /**
     * Obtiene todas las sesiones activas de un usuario específico.
     *
     * @param userId ID del usuario en Keycloak
     * @return Lista de sesiones activas con información detallada (IP, timestamps, clientes)
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getActiveSessions(@PathVariable String userId) {
        try {
            log.info("Obteniendo sesiones activas para usuario: {}", userId);
            GetActiveSessionsQuery query = new GetActiveSessionsQuery(userId);
            GetActiveSessionsResponse response = mediator.send(query);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error al obtener sesiones para usuario {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(ApiResponse.fail(
                    com.kynsof.share.core.domain.response.ApiError.withSingleError(
                        "error", "sessions", "Error al obtener sesiones activas: " + e.getMessage()
                    )
                ));
        }
    }

    /**
     * Elimina una sesión específica de un usuario.
     * Útil para cerrar sesiones sospechosas o desde dispositivos no autorizados.
     *
     * @param userId ID del usuario en Keycloak
     * @param sessionId ID de la sesión a eliminar
     * @return Confirmación de eliminación
     */
    @DeleteMapping("/{userId}/{sessionId}")
    public ResponseEntity<?> deleteSession(
        @PathVariable String userId,
        @PathVariable String sessionId
    ) {
        try {
            log.info("Eliminando sesión {} del usuario {}", sessionId, userId);
            DeleteSessionCommand command = new DeleteSessionCommand(userId, sessionId);
            DeleteSessionMessage response = mediator.send(command);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error al eliminar sesión {} del usuario {}: {}", sessionId, userId, e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(ApiResponse.fail(
                    com.kynsof.share.core.domain.response.ApiError.withSingleError(
                        "error", "session", "Error al eliminar la sesión: " + e.getMessage()
                    )
                ));
        }
    }

    /**
     * Elimina todas las sesiones activas de un usuario (logout forzado).
     * Útil para casos de seguridad o cuando el usuario cambia su contraseña.
     *
     * @param userId ID del usuario en Keycloak
     * @return Confirmación de eliminación masiva
     */
    @DeleteMapping("/{userId}/all")
    public ResponseEntity<?> deleteAllSessions(@PathVariable String userId) {
        try {
            log.info("Eliminando todas las sesiones del usuario {}", userId);
            DeleteAllSessionsCommand command = new DeleteAllSessionsCommand(userId);
            DeleteAllSessionsMessage response = mediator.send(command);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("Error al eliminar todas las sesiones del usuario {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(ApiResponse.fail(
                    com.kynsof.share.core.domain.response.ApiError.withSingleError(
                        "error", "sessions", "Error al eliminar todas las sesiones: " + e.getMessage()
                    )
                ));
        }
    }
}
