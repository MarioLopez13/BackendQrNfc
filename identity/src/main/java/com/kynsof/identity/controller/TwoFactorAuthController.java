package com.kynsof.identity.controller;

import com.kynsof.identity.application.command.twofactor.delete.Delete2FAByIdCommand;
import com.kynsof.identity.application.command.twofactor.delete.Delete2FAByIdMessage;
import com.kynsof.identity.application.command.twofactor.disable.Disable2FACommand;
import com.kynsof.identity.application.command.twofactor.disable.Disable2FAMessage;
import com.kynsof.identity.application.command.twofactor.disable.Disable2FARequest;
import com.kynsof.identity.application.command.twofactor.setup.Setup2FACommand;
import com.kynsof.identity.application.command.twofactor.setup.Setup2FAMessage;
import com.kynsof.identity.application.command.twofactor.verify.Verify2FACommand;
import com.kynsof.identity.application.command.twofactor.verify.Verify2FAMessage;
import com.kynsof.identity.application.command.twofactor.verify.Verify2FARequest;
import com.kynsof.identity.application.command.twofactor.verifylogin.VerifyLogin2FACommand;
import com.kynsof.identity.application.command.twofactor.verifylogin.VerifyLogin2FAMessage;
import com.kynsof.identity.application.command.twofactor.verifylogin.VerifyLogin2FARequest;
import com.kynsof.identity.application.query.twofactor.search.GetSearchTwoFactorAuthQuery;
import com.kynsof.identity.application.query.twofactor.status.Get2FAStatusQuery;
import com.kynsof.identity.application.query.twofactor.status.Get2FAStatusResponse;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.request.PageableUtil;
import com.kynsof.share.core.domain.request.SearchRequest;
import com.kynsof.share.core.domain.response.ApiResponse;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.bus.IMediator;
import org.springframework.data.domain.Pageable;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.UUID;

/**
 * Controlador para gestionar la autenticación de doble factor (2FA).
 * Permite a los usuarios configurar, verificar y desactivar 2FA usando Google Authenticator.
 */
@RestController
@RequestMapping("/api/2fa")
public class TwoFactorAuthController {

    private final IMediator mediator;

    public TwoFactorAuthController(IMediator mediator) {
        this.mediator = mediator;
    }

    /**
     * Inicia el proceso de configuración de 2FA.
     * Genera un secreto y devuelve el QR code para escanear con Google Authenticator.
     *
     * @param jwt Token JWT del usuario autenticado
     * @return Setup con secreto y QR code en base64
     */
    @PreAuthorize("isAuthenticated()")
    @RateLimiter(name = "twoFactorLimit")
    @PostMapping("/setup")
    public Mono<ResponseEntity<ApiResponse<Setup2FAMessage>>> setup(@AuthenticationPrincipal Jwt jwt) {
        return Mono.fromCallable(() -> {
            UUID userId = UUID.fromString(jwt.getSubject());
            String email = jwt.getClaimAsString("email");

            Setup2FACommand command = new Setup2FACommand(userId, email);
            mediator.send(command);

            return ResponseEntity.ok(ApiResponse.success((Setup2FAMessage) command.getMessage()));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Verifica el código TOTP y activa 2FA.
     * Devuelve los códigos de respaldo que el usuario debe guardar.
     *
     * @param jwt Token JWT del usuario autenticado
     * @param request Código de 6 dígitos del autenticador
     * @return Códigos de respaldo
     */
    @PreAuthorize("isAuthenticated()")
    @RateLimiter(name = "twoFactorLimit")
    @PostMapping("/verify")
    public Mono<ResponseEntity<ApiResponse<Verify2FAMessage>>> verify(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Verify2FARequest request) {
        return Mono.fromCallable(() -> {
            UUID userId = UUID.fromString(jwt.getSubject());
            request.setUserId(userId);

            Verify2FACommand command = Verify2FACommand.fromRequest(request);
            mediator.send(command);

            return ResponseEntity.ok(ApiResponse.success((Verify2FAMessage) command.getMessage()));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Desactiva 2FA para el usuario.
     * Requiere el código TOTP actual para confirmar.
     *
     * @param jwt Token JWT del usuario autenticado
     * @param request Código de 6 dígitos para confirmar
     * @return Mensaje de éxito
     */
    @PreAuthorize("isAuthenticated()")
    @RateLimiter(name = "twoFactorLimit")
    @DeleteMapping
    public Mono<ResponseEntity<ApiResponse<Disable2FAMessage>>> disable(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Disable2FARequest request) {
        return Mono.fromCallable(() -> {
            UUID userId = UUID.fromString(jwt.getSubject());
            request.setUserId(userId);

            Disable2FACommand command = Disable2FACommand.fromRequest(request);
            mediator.send(command);

            return ResponseEntity.ok(ApiResponse.success((Disable2FAMessage) command.getMessage()));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Obtiene el estado de 2FA del usuario.
     *
     * @param jwt Token JWT del usuario autenticado
     * @return Estado de 2FA (habilitado, configurado, códigos restantes)
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/status")
    public Mono<ResponseEntity<ApiResponse<Get2FAStatusResponse>>> getStatus(@AuthenticationPrincipal Jwt jwt) {
        return Mono.fromCallable(() -> {
            UUID userId = UUID.fromString(jwt.getSubject());

            Get2FAStatusQuery query = new Get2FAStatusQuery(userId);
            Get2FAStatusResponse response = mediator.send(query);

            return ResponseEntity.ok(ApiResponse.success(response));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Verifica el código 2FA durante el login.
     * Este endpoint se usa después de que el usuario ha iniciado sesión
     * y necesita completar la verificación de 2FA.
     *
     * @param request UserId, código y si es código de respaldo
     * @return Resultado de la verificación
     */
    @PreAuthorize("permitAll()")
    @RateLimiter(name = "twoFactorLoginLimit")
    @PostMapping("/verify-login")
    public Mono<ResponseEntity<ApiResponse<VerifyLogin2FAMessage>>> verifyLogin(
            @RequestBody VerifyLogin2FARequest request) {
        return Mono.fromCallable(() -> {
            VerifyLogin2FACommand command = VerifyLogin2FACommand.fromRequest(request);
            mediator.send(command);

            return ResponseEntity.ok(ApiResponse.success((VerifyLogin2FAMessage) command.getMessage()));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Verifica si un usuario tiene 2FA habilitado.
     * Este endpoint es público y se usa para determinar si mostrar
     * el paso de verificación 2FA después del login.
     *
     * @param userId ID del usuario
     * @return true si 2FA está habilitado
     */
    @PreAuthorize("permitAll()")
    @GetMapping("/check/{userId}")
    public Mono<ResponseEntity<ApiResponse<Boolean>>> checkTwoFactorEnabled(@PathVariable UUID userId) {
        return Mono.fromCallable(() -> {
            Get2FAStatusQuery query = new Get2FAStatusQuery(userId);
            Get2FAStatusResponse response = mediator.send(query);

            return ResponseEntity.ok(ApiResponse.success(response.isEnabled()));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Busca todos los registros de 2FA con paginación y filtros.
     * Solo para administradores.
     *
     * @param request Parámetros de búsqueda y filtros
     * @return Lista paginada de registros de 2FA
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/admin/search")
    public ResponseEntity<ApiResponse<PaginatedResponse>> search(@RequestBody SearchRequest request) {
        Pageable pageable = PageableUtil.createPageable(request);
        List<FilterCriteria> filter = request.getFilter() != null ? request.getFilter() : List.of();

        GetSearchTwoFactorAuthQuery query = new GetSearchTwoFactorAuthQuery(pageable, filter, request.getQuery());
        PaginatedResponse response = mediator.send(query);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Elimina un registro de 2FA por ID.
     * Solo para administradores. Esto desactiva el 2FA para el usuario asociado.
     *
     * @param id ID del registro de 2FA
     * @return Mensaje de éxito
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<ApiResponse<Delete2FAByIdMessage>> deleteById(@PathVariable UUID id) {
        Delete2FAByIdCommand command = new Delete2FAByIdCommand(id);
        mediator.send(command);

        return ResponseEntity.ok(ApiResponse.success((Delete2FAByIdMessage) command.getMessage()));
    }
}
