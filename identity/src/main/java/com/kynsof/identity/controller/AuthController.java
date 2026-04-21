package com.kynsof.identity.controller;

import com.kynsof.identity.application.command.auth.TokenRefreshRequest;
import com.kynsof.identity.application.command.auth.autenticate.*;
import com.kynsof.identity.application.command.auth.deletedAccount.DeleteAccountCommand;
import com.kynsof.identity.application.command.auth.deletedAccount.DeleteAccountMessage;
import com.kynsof.identity.application.command.auth.emailLogin.request.EmailLoginRequestCommand;
import com.kynsof.identity.application.command.auth.emailLogin.request.EmailLoginRequestMessage;
import com.kynsof.identity.application.command.auth.emailLogin.request.EmailLoginRequestRequest;
import com.kynsof.identity.application.command.auth.emailLogin.verify.EmailLoginVerifyCommand;
import com.kynsof.identity.application.command.auth.emailLogin.verify.EmailLoginVerifyMessage;
import com.kynsof.identity.application.command.auth.emailLogin.verify.EmailLoginVerifyRequest;
import com.kynsof.identity.application.command.auth.exchangeCode.ExchangeCodeCommand;
import com.kynsof.identity.application.command.auth.exchangeCode.ExchangeCodeRequest;
import com.kynsof.identity.application.command.auth.registerGoogle.RegisterGoogleUserCommand;
import com.kynsof.identity.application.command.auth.registerGoogle.RegisterGoogleUserMessage;
import com.kynsof.identity.application.command.auth.registerGoogle.RegisterGoogleUserRequest;
import com.kynsof.identity.application.command.auth.firstsChangePassword.FirstsChangePasswordCommand;
import com.kynsof.identity.application.command.auth.firstsChangePassword.FirstsChangePasswordMessage;
import com.kynsof.identity.application.command.auth.firstsChangePassword.FirstsChangePasswordRequest;
import com.kynsof.identity.application.command.auth.forwardPassword.ForwardPasswordCommand;
import com.kynsof.identity.application.command.auth.forwardPassword.ForwardPasswordMessage;
import com.kynsof.identity.application.command.auth.forwardPassword.PasswordChangeRequest;
import com.kynsof.identity.application.command.auth.registry.RegistryCommand;
import com.kynsof.identity.application.command.auth.registry.RegistryMessage;
import com.kynsof.identity.application.command.auth.registry.UserRequest;
import com.kynsof.identity.application.command.auth.sendPasswordRecoveryOtp.SendPasswordRecoveryOtpCommand;
import com.kynsof.identity.application.command.auth.sendPasswordRecoveryOtp.SendPasswordRecoveryOtpMessage;
import com.kynsof.identity.application.command.auth.resendOtp.ResendOtpCommand;
import com.kynsof.identity.application.command.auth.resendOtp.ResendOtpMessage;
import com.kynsof.identity.application.command.auth.resendOtp.ResendOtpRequest;
import com.kynsof.identity.application.command.auth.syncGoogleUser.SyncGoogleUserCommand;
import com.kynsof.identity.application.command.auth.syncGoogleUser.SyncGoogleUserMessage;
import com.kynsof.identity.application.command.auth.syncGoogleUser.SyncGoogleUserRequest;
import com.kynsof.identity.application.query.auth.RefreshTokenQuery;
import com.kynsof.identity.application.query.users.existByEmail.ExistByEmailUserSystemsQuery;
import com.kynsof.identity.application.query.users.existByEmail.UserSystemsExistByEmailResponse;
import com.kynsof.share.core.domain.response.ApiError;
import com.kynsof.share.core.domain.response.ApiResponse;
import com.kynsof.share.core.infrastructure.bus.IMediator;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Controlador para gestionar autenticación y operaciones relacionadas con la identidad de usuario.
 * Implementa rate limiting consistente para proteger los endpoints sensibles contra ataques de fuerza bruta.
 */
/**@RestController
@RequestMapping("/api/auth")
public class AuthController {*/

@RestController
@CrossOrigin(originPatterns = "http://localhost:*")
@RequestMapping("/api/auth")
public class AuthController {

    private final IMediator mediator;

    @Value("${app.version}")
    private String appVersion;

    public AuthController(IMediator mediator) {
        this.mediator = mediator;
    }

    /**
     * Endpoint para intercambio de código OAuth2 por token de Keycloak (Google, SSO, etc)
     * POST /api/auth/exchange-code
     * Body: { "code": "...", "redirectUri": "..." }
     */
    @PostMapping("/exchange-code")
    public Mono<ResponseEntity<?>> exchangeCode(@RequestBody ExchangeCodeRequest request) {
        return Mono.fromCallable(() -> {
            ExchangeCodeCommand command = new ExchangeCodeCommand(request.getCode(), request.getRedirectUri());
            mediator.send(command);
            return command.getTokenResponse();
        })
        .subscribeOn(Schedulers.boundedElastic())
        .map(tokenResponse -> {
            if (tokenResponse.getError() != null) {
                ApiError apiError = ApiError.withSingleError(
                    tokenResponse.getErrorDescription(),
                    tokenResponse.getErrorField(),
                    tokenResponse.getErrorMessage()
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.fail(apiError));
            }
            return ResponseEntity.ok(ApiResponse.success(tokenResponse));
        });
    }

    /**
     * Endpoint para registrar/sincronizar usuario que se autentica vía Google OAuth2.
     *
     * Flujo:
     * 1. Frontend recibe código de autorización de Google
     * 2. Llama a este endpoint con el código y redirectUri
     * 3. Backend intercambia código con Keycloak → Keycloak valida con Google
     * 4. Backend decodifica el token JWT recibido y extrae info del usuario
     * 5. Backend busca si el usuario ya existe en BD local
     * 6. Si existe: retorna su información
     * 7. Si no existe: crea usuario nuevo
     * 8. Retorna información del usuario (nuevo o existente)
     *
     * POST /api/auth/register-google
     * Body: { "code": "4/0AY...", "redirectUri": "https://app.com/callback" }
     * Response: { "userId": "uuid", "email": "...", "name": "...", "lastName": "...", "isNewUser": true/false }
     */
    @PreAuthorize("permitAll()")
    @RateLimiter(name = "registrationLimit", fallbackMethod = "registrationFallback")
    @PostMapping("/register-google")
    public Mono<ResponseEntity<?>> registerGoogleUser(@RequestBody RegisterGoogleUserRequest request) {
        return Mono.fromCallable(() -> {
                    try {
                        RegisterGoogleUserCommand command = new RegisterGoogleUserCommand(
                                request.getCode(),
                                request.getRedirectUri()
                        );
                        mediator.send(command);
                        RegisterGoogleUserMessage response = (RegisterGoogleUserMessage) command.getMessage();
                        return (ResponseEntity<?>) ResponseEntity.ok(ApiResponse.success(response));
                    } catch (Exception e) {
                        ApiError apiError = ApiError.withSingleError(
                                "Error al procesar registro de Google",
                                "google",
                                e.getMessage()
                        );
                        return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.fail(apiError));
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Endpoint para sincronizar usuario de Google que YA EXISTE en Keycloak.
     *
     * Este endpoint se usa cuando el usuario ya se registró vía Google OAuth en Keycloak
     * y necesita ser sincronizado/creado en la base de datos local de identity.
     *
     * Diferencia con /register-google:
     * - Este endpoint busca también por email si no encuentra por keyCloakId
     * - Si el usuario existe por email pero con keyCloakId diferente, actualiza el keyCloakId
     * - Ideal para usuarios que ya estaban en el sistema y ahora vinculan Google
     *
     * Flujo:
     * 1. Frontend recibe código de autorización de Google (usuario ya existe en Keycloak)
     * 2. Llama a este endpoint con el código y redirectUri
     * 3. Backend intercambia código con Keycloak para obtener tokens
     * 4. Backend busca usuario por keyCloakId en BD local
     * 5. Si no existe por keyCloakId, busca por email
     * 6. Si existe por email: actualiza keyCloakId y retorna info
     * 7. Si no existe: crea usuario nuevo
     *
     * POST /api/auth/sync-google-user
     * Body: { "code": "4/0AY...", "redirectUri": "https://app.com/callback" }
     * Response: {
     *   "userId": "uuid",
     *   "email": "...",
     *   "name": "...",
     *   "lastName": "...",
     *   "isNewUser": true/false,
     *   "keyCloakIdUpdated": true/false
     * }
     */
    @PreAuthorize("permitAll()")
    @RateLimiter(name = "registrationLimit", fallbackMethod = "registrationFallback")
    @PostMapping("/sync-google-user")
    public Mono<ResponseEntity<?>> syncGoogleUser(
            @RequestBody SyncGoogleUserRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return Mono.fromCallable(() -> {
                    try {
                        // Extraer el token del header Authorization (Bearer xxx)
                        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                            String token = authorizationHeader.substring(7);
                            request.setAccessToken(token);
                        }
                        SyncGoogleUserCommand command = SyncGoogleUserCommand.fromRequest(request);
                        mediator.send(command);
                        SyncGoogleUserMessage response = (SyncGoogleUserMessage) command.getMessage();
                        return (ResponseEntity<?>) ResponseEntity.ok(ApiResponse.success(response));
                    } catch (Exception e) {
                        ApiError apiError = ApiError.withSingleError(
                                "Error al sincronizar usuario de Google",
                                "google",
                                e.getMessage()
                        );
                        return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.fail(apiError));
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Endpoint para autenticación de usuarios.
     * Protegido con rate limiting para prevenir ataques de fuerza bruta.
     */
  //  @RateLimiter(name = "authenticationLimit", fallbackMethod = "authenticateFallback")
    @PostMapping("/authenticate")
    public Mono<ResponseEntity<?>> authenticate(@RequestBody LoginRequest loginDTO) {
        return Mono.fromCallable(() -> {
                    AuthenticateCommand authenticateCommand = new AuthenticateCommand(loginDTO.getUsername(), loginDTO.getPassword());
                    return (AuthenticateMessage) mediator.send(authenticateCommand);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(response -> {
                    TokenResponse tokenResponse = response.getTokenResponse();
                    if (tokenResponse.getError() != null) {
                        if ("password_change_required".equals(tokenResponse.getError())) {
                            ApiError apiError = ApiError.withSingleError(
                                tokenResponse.getErrorDescription(),
                                tokenResponse.getErrorField(),
                                tokenResponse.getErrorMessage()
                            );
                            apiError.setStatus(428);
                            return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED)
                                    .body(ApiResponse.fail(apiError));
                        }
                        ApiError apiError = ApiError.withSingleError(
                            tokenResponse.getErrorDescription(),
                            tokenResponse.getErrorField(),
                            tokenResponse.getErrorMessage()
                        );
                        return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.fail(apiError));
                    }
                    return (ResponseEntity<?>) ResponseEntity.ok(tokenResponse);
                });
    }


    /**
     * Endpoint para eliminar cuenta de usuario.
     * Protegido con rate limiting para prevenir ataques de eliminación masiva.
     */
    @RateLimiter(name = "accountOperationsLimit", fallbackMethod = "accountOperationsFallback")
    @PostMapping("/delete-account")
    public Mono<ResponseEntity<?>> deleteAccount(@RequestBody DeleteRequest loginDTO) {
        return Mono.fromCallable(() -> {
            DeleteAccountCommand command = new DeleteAccountCommand(loginDTO.getUsername(), loginDTO.getPassword());
            DeleteAccountMessage response = this.mediator.send(command);
            return ResponseEntity.ok(ApiResponse.success(response.getResult()));
        })
        .subscribeOn(Schedulers.boundedElastic())
        .map(r -> (ResponseEntity<?>) r);
    }

    /**
     * Endpoint para el primer cambio de contraseña (generalmente después de registro).
     * Protegido con rate limiting específico para cambios de contraseña.
     */
    @PreAuthorize("permitAll()")
    @RateLimiter(name = "passwordChangeLimit", fallbackMethod = "passwordOperationsFallback")
    @PostMapping("/firsts-change-password")
    public Mono<ResponseEntity<?>> firstsChangePassword(@RequestBody FirstsChangePasswordRequest request) {
        return Mono.fromCallable(() -> {
            FirstsChangePasswordCommand authenticateCommand = FirstsChangePasswordCommand.fromRequest(request);
            FirstsChangePasswordMessage response = mediator.send(authenticateCommand);
            return ResponseEntity.ok(response.getResult());
        })
        .subscribeOn(Schedulers.boundedElastic())
        .map(r -> (ResponseEntity<?>) r);
    }

    /**
     * Endpoint para registro de nuevos usuarios.
     * Protegido con rate limiting para prevenir registros masivos automatizados.
     */
    @PreAuthorize("permitAll()")
    @RateLimiter(name = "registrationLimit", fallbackMethod = "registrationFallback")
    @PostMapping("/register")
    public Mono<ResponseEntity<ApiResponse<String>>> registerUser(@RequestBody UserRequest userRequest) {
        return Mono.fromCallable(() -> {
            RegistryCommand command = new RegistryCommand(userRequest.getUserName(), userRequest.getEmail(), userRequest.getName(),
                    userRequest.getLastName(), userRequest.getPassword(), null);
            RegistryMessage registryMessage = mediator.send(command);
            return ResponseEntity.ok(ApiResponse.success(registryMessage.getResult()));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Endpoint para refrescar tokens de autenticación.
     * Protegido con rate limiting para prevenir abusos.
     */
    @RateLimiter(name = "authenticationLimit", fallbackMethod = "tokenOperationsFallback")
    @PostMapping("/refresh-token")
    public Mono<ResponseEntity<?>> refreshToken(@RequestBody TokenRefreshRequest request) {
        return Mono.fromCallable(() -> {
            try {
                RefreshTokenQuery query = new RefreshTokenQuery(request.getRefreshToken());
                TokenResponse response = mediator.send(query);
                return (ResponseEntity<?>) ResponseEntity.ok(ApiResponse.success(response));
            } catch (Exception e) {
                ApiError apiError = ApiError.withSingleError(
                        "Token expirado o inválido",
                        "refreshToken",
                        e.getMessage() != null ? e.getMessage() : "Refresh token is invalid or expired"
                );
                return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.fail(apiError));
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Endpoint para recuperación de contraseña olvidada.
     * Protegido con rate limiting para prevenir ataques de enumeración de usuarios.
     */
    @RateLimiter(name = "passwordRecoveryLimit", fallbackMethod = "passwordOperationsFallback")
    @PostMapping("/forgot-password")
    public Mono<ResponseEntity<?>> forgotPassword(@RequestParam String email) {
        return Mono.fromCallable(() -> {
            SendPasswordRecoveryOtpCommand command = new SendPasswordRecoveryOtpCommand(email);
            SendPasswordRecoveryOtpMessage sendPasswordRecoveryOtpMessage = mediator.send(command);
            return ResponseEntity.ok(ApiResponse.success(sendPasswordRecoveryOtpMessage.getResult()));
        })
        .subscribeOn(Schedulers.boundedElastic())
        .map(r -> (ResponseEntity<?>) r);
    }

    /**
     * Endpoint para solicitar código de inicio de sesión por email (passwordless login).
     * Envía un código OTP al email del usuario para autenticación sin contraseña.
     *
     * Flujo:
     * 1. Usuario proporciona su email
     * 2. Sistema verifica que el usuario existe
     * 3. Sistema genera y envía código OTP por email
     * 4. Usuario usa el código en /email-login/verify para obtener el token
     *
     * POST /api/auth/email-login/request
     * Body: { "email": "usuario@example.com" }
     */
    @PreAuthorize("permitAll()")
    //@RateLimiter(name = "passwordRecoveryLimit", fallbackMethod = "emailLoginFallback")
    @PostMapping("/email-login/request")
    public Mono<ResponseEntity<?>> emailLoginRequest(@RequestBody EmailLoginRequestRequest request) {
        return Mono.fromCallable(() -> {
            try {
                EmailLoginRequestCommand command = EmailLoginRequestCommand.fromRequest(request);
                EmailLoginRequestMessage response = mediator.send(command);
                return (ResponseEntity<?>) ResponseEntity.ok(ApiResponse.success(response.getResult()));
            } catch (Exception e) {
                ApiError apiError = ApiError.withSingleError(
                        "Error al procesar la solicitud",
                        "email",
                        e.getMessage()
                );
                return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.fail(apiError));
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Endpoint para verificar código OTP y obtener token (passwordless login).
     * Valida el código enviado por email y retorna un token JWT de autenticación.
     *
     * Flujo:
     * 1. Usuario proporciona email y código OTP recibido
     * 2. Sistema valida el código contra Redis
     * 3. Sistema realiza Token Exchange con Keycloak
     * 4. Sistema retorna access_token y refresh_token
     *
     * POST /api/auth/email-login/verify
     * Body: { "email": "usuario@example.com", "code": "123456" }
     * Response: TokenResponse con access_token y refresh_token
     */
    @PreAuthorize("permitAll()")
    @RateLimiter(name = "authenticationLimit", fallbackMethod = "emailLoginVerifyFallback")
    @PostMapping("/email-login/verify")
    public Mono<ResponseEntity<?>> emailLoginVerify(@RequestBody EmailLoginVerifyRequest request) {
        return Mono.fromCallable(() -> {
            try {
                EmailLoginVerifyCommand command = EmailLoginVerifyCommand.fromRequest(request);
                EmailLoginVerifyMessage response = mediator.send(command);
                TokenResponse tokenResponse = response.getTokenResponse();

                if (tokenResponse.getError() != null) {
                    ApiError apiError = ApiError.withSingleError(
                            tokenResponse.getErrorDescription(),
                            tokenResponse.getErrorField(),
                            tokenResponse.getErrorMessage()
                    );
                    return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(ApiResponse.fail(apiError));
                }

                return (ResponseEntity<?>) ResponseEntity.ok(tokenResponse);
            } catch (Exception e) {
                ApiError apiError = ApiError.withSingleError(
                        "Error al verificar el código",
                        "code",
                        e.getMessage()
                );
                return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.fail(apiError));
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Endpoint para cambio de contraseña.
     * Protegido con rate limiting específico para cambios de contraseña.
     */
    @RateLimiter(name = "passwordChangeLimit", fallbackMethod = "passwordOperationsFallback")
    @PostMapping("/change-password")
    public Mono<ResponseEntity<?>> changePassword(@RequestBody PasswordChangeRequest request) {
        return Mono.fromCallable(() -> {
            ForwardPasswordCommand command = new ForwardPasswordCommand(request.getEmail(), request.getNewPassword(),
                    request.getOtp());
            ForwardPasswordMessage response = mediator.send(command);
            return ResponseEntity.ok(ApiResponse.success(response.getResult()));
        })
        .subscribeOn(Schedulers.boundedElastic())
        .map(r -> (ResponseEntity<?>) r);
    }

    /**
     * Endpoint para reenviar código OTP al usuario durante el primer cambio de contraseña.
     * Protegido con rate limiting para prevenir abuso del sistema de OTP.
     */
    @PreAuthorize("permitAll()")
    @RateLimiter(name = "passwordRecoveryLimit", fallbackMethod = "passwordOperationsFallback")
    @PostMapping("/resend-otp")
    public Mono<ResponseEntity<?>> resendOtp(@RequestBody ResendOtpRequest request) {
        return Mono.fromCallable(() -> {
            ResendOtpCommand command = ResendOtpCommand.fromRequest(request);
            ResendOtpMessage response = mediator.send(command);
            return ResponseEntity.ok(ApiResponse.success(response.getResult()));
        })
        .subscribeOn(Schedulers.boundedElastic())
        .map(r -> (ResponseEntity<?>) r);
    }

    /**
     * Endpoint para verificar la versión de la aplicación.
     * No requiere rate limiting al ser información no sensible.
     */
    @GetMapping("/app-version")
    public Mono<ResponseEntity<?>> appVersion() {
        return Mono.just(ResponseEntity.ok(ApiResponse.success(appVersion)));
    }

    /**
     * Endpoint para verificar la existencia de un usuario por email.
     * Protegido con rate limiting para prevenir ataques de enumeración de usuarios.
     */
    @RateLimiter(name = "emailCheckLimit", fallbackMethod = "emailCheckFallback")
    @GetMapping("/exist-by-email/{email}")
    public Mono<ResponseEntity<UserSystemsExistByEmailResponse>> existUserByEmail(@PathVariable String email) {
        return Mono.fromCallable(() -> {
            ExistByEmailUserSystemsQuery query = new ExistByEmailUserSystemsQuery(email);
            UserSystemsExistByEmailResponse response = mediator.send(query);
            return ResponseEntity.ok(response);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Método fallback para el endpoint de autenticación cuando se excede el rate limit.
     */
    public Mono<ResponseEntity<?>> authenticateFallback(LoginRequest request, RequestNotPermitted ex) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Retry-After", "60");

        ApiError apiError = new ApiError("Demasiados intentos de inicio de sesión. Por favor, inténtelo más tarde.");
        return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(responseHeaders)
                .body(ApiResponse.fail(apiError)));
    }

    /**
     * Métodos fallback para diferentes operaciones cuando se exceden los rate limits
     */
    public Mono<ResponseEntity<?>> accountOperationsFallback(DeleteRequest request, RequestNotPermitted ex) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Retry-After", "300");

        ApiError apiError = new ApiError("Demasiados intentos de operaciones con la cuenta. Por favor, inténtelo más tarde.");
        return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(responseHeaders)
                .body(ApiResponse.fail(apiError)));
    }

    public Mono<ResponseEntity<?>> passwordOperationsFallback(Object request, RequestNotPermitted ex) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Retry-After", "300");

        ApiError apiError = new ApiError("Demasiados intentos de operaciones con contraseñas. Por favor, inténtelo más tarde.");
        return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(responseHeaders)
                .body(ApiResponse.fail(apiError)));
    }

    public Mono<ResponseEntity<?>> registrationFallback(Object request, RequestNotPermitted ex) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Retry-After", "3600");

        ApiError apiError = new ApiError("Demasiados intentos de registro. Por favor, inténtelo más tarde.");
        return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(responseHeaders)
                .body(ApiResponse.fail(apiError)));
    }

    public Mono<ResponseEntity<?>> tokenOperationsFallback(TokenRefreshRequest request, RequestNotPermitted ex) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Retry-After", "60");

        ApiError apiError = new ApiError("Demasiados intentos de operaciones con tokens. Por favor, inténtelo más tarde.");
        return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(responseHeaders)
                .body(ApiResponse.fail(apiError)));
    }

    public Mono<ResponseEntity<?>> emailCheckFallback(String email, RequestNotPermitted ex) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Retry-After", "60");

        ApiError apiError = new ApiError("Demasiadas verificaciones de email. Por favor, inténtelo más tarde.");
        return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(responseHeaders)
                .body(ApiResponse.fail(apiError)));
    }

    /**
     * Método fallback para el endpoint de solicitud de login por email cuando se excede el rate limit.
     */
    public Mono<ResponseEntity<?>> emailLoginFallback(EmailLoginRequestRequest request, RequestNotPermitted ex) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Retry-After", "60");

        ApiError apiError = new ApiError("Demasiados intentos de inicio de sesión por email. Por favor, inténtelo más tarde.");
        return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(responseHeaders)
                .body(ApiResponse.fail(apiError)));
    }

    /**
     * Método fallback para el endpoint de verificación de login por email cuando se excede el rate limit.
     */
    public Mono<ResponseEntity<?>> emailLoginVerifyFallback(EmailLoginVerifyRequest request, RequestNotPermitted ex) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Retry-After", "60");

        ApiError apiError = new ApiError("Demasiados intentos de verificación. Por favor, inténtelo más tarde.");
        return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .headers(responseHeaders)
                .body(ApiResponse.fail(apiError)));
    }
}
