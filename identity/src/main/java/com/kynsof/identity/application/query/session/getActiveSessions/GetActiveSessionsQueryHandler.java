package com.kynsof.identity.application.query.session.getActiveSessions;

import com.kynsof.identity.domain.dto.UserSessionDto;
import com.kynsof.identity.infrastructure.services.KeycloakProvider;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserSessionRepresentation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GetActiveSessionsQueryHandler implements IQueryHandler<GetActiveSessionsQuery, GetActiveSessionsResponse> {

    private final KeycloakProvider keycloakProvider;

    public GetActiveSessionsQueryHandler(KeycloakProvider keycloakProvider) {
        this.keycloakProvider = keycloakProvider;
    }

    @Override
    public GetActiveSessionsResponse handle(GetActiveSessionsQuery query) {
        try {
            // Obtener sesiones activas del usuario desde Keycloak
            List<UserSessionRepresentation> sessions = keycloakProvider.withUsers(users ->
                users.get(query.getUserId()).getUserSessions()
            );

            // Mapear a nuestro DTO
            List<UserSessionDto> sessionDtos = sessions.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

            log.info("Sesiones activas encontradas para usuario {}: {}", query.getUserId(), sessionDtos.size());

            return new GetActiveSessionsResponse(sessionDtos, sessionDtos.size());
        } catch (Exception e) {
            log.error("Error al obtener sesiones activas para usuario {}: {}", query.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Error al obtener sesiones activas: " + e.getMessage(), e);
        }
    }

    private UserSessionDto mapToDto(UserSessionRepresentation session) {
        UserSessionDto dto = new UserSessionDto();
        dto.setId(session.getId());
        dto.setUserId(session.getUserId());
        dto.setUsername(session.getUsername());
        dto.setIpAddress(session.getIpAddress());
        dto.setStart(session.getStart());
        dto.setLastAccess(session.getLastAccess());
        dto.setClients(session.getClients());
        return dto;
    }
}
