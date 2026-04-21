package com.kynsof.identity.infrastructure.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.kynsof.identity.domain.dto.TwoFactorAuthDto;
import com.kynsof.identity.domain.dto.TwoFactorSetupDto;
import com.kynsof.identity.domain.dto.UserSystemDto;
import com.kynsof.identity.domain.interfaces.service.ITwoFactorAuthService;
import com.kynsof.identity.domain.interfaces.service.IUserSystemService;
import com.kynsof.identity.infrastructure.entities.TwoFactorAuth;
import com.kynsof.identity.infrastructure.repository.command.TwoFactorAuthWriteRepository;
import com.kynsof.identity.infrastructure.repository.query.TwoFactorAuthReadRepository;
import com.kynsof.share.core.domain.exception.BusinessException;
import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.specifications.GenericSpecificationsBuilder;
import com.kynsof.identity.application.query.twofactor.search.TwoFactorAuthListResponse;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TwoFactorAuthService implements ITwoFactorAuthService {

    private static final String ISSUER = "DoctorKyn";
    private static final int BACKUP_CODES_COUNT = 8;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 15;

    private final TwoFactorAuthReadRepository readRepository;
    private final TwoFactorAuthWriteRepository writeRepository;
    private final IUserSystemService userSystemService;
    private final GoogleAuthenticator googleAuthenticator;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    @Value("${twofactor.issuer:DoctorKyn}")
    private String issuer;

    public TwoFactorAuthService(
            TwoFactorAuthReadRepository readRepository,
            TwoFactorAuthWriteRepository writeRepository,
            IUserSystemService userSystemService) {
        this.readRepository = readRepository;
        this.writeRepository = writeRepository;
        this.userSystemService = userSystemService;
        this.googleAuthenticator = new GoogleAuthenticator();
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.secureRandom = new SecureRandom();
    }

    @Override
    @Transactional
    public TwoFactorSetupDto setupTwoFactor(UUID userId, String email) {
        // Verificar si ya tiene 2FA activado
        Optional<TwoFactorAuth> existing = readRepository.findByUserId(userId);
        if (existing.isPresent() && existing.get().isEnabled()) {
            throw new BusinessException(DomainErrorMessage.TWO_FACTOR_ALREADY_ENABLED,
                    "El usuario ya tiene 2FA activado");
        }

        // Generar nuevo secreto
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        String secret = key.getKey();

        // Generar URL para Google Authenticator
        String otpAuthUrl = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                issuer != null ? issuer : ISSUER,
                email,
                key
        );

        // Generar QR code en base64
        String qrCodeBase64 = generateQRCodeBase64(otpAuthUrl);

        // Guardar o actualizar el registro
        TwoFactorAuth twoFactorAuth;
        if (existing.isPresent()) {
            twoFactorAuth = existing.get();
            twoFactorAuth.setSecretKey(secret);
            twoFactorAuth.setVerified(false);
            twoFactorAuth.setEnabled(false);
            twoFactorAuth.setFailedAttempts(0);
            twoFactorAuth.setLockedUntil(null);
        } else {
            twoFactorAuth = new TwoFactorAuth();
            twoFactorAuth.setId(UUID.randomUUID());
            twoFactorAuth.setUserId(userId);
            twoFactorAuth.setSecretKey(secret);
            twoFactorAuth.setEnabled(false);
            twoFactorAuth.setVerified(false);
        }

        writeRepository.save(twoFactorAuth);

        return new TwoFactorSetupDto(secret, qrCodeBase64, otpAuthUrl);
    }

    @Override
    @Transactional
    public String[] verifyAndEnableTwoFactor(UUID userId, String code) {
        TwoFactorAuth twoFactorAuth = readRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(DomainErrorMessage.TWO_FACTOR_NOT_CONFIGURED,
                        "Debe iniciar el setup de 2FA primero"));

        if (twoFactorAuth.isEnabled()) {
            throw new BusinessException(DomainErrorMessage.TWO_FACTOR_ALREADY_ENABLED,
                    "El 2FA ya esta activado");
        }

        // Verificar el codigo
        int codeInt;
        try {
            codeInt = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            throw new BusinessException(DomainErrorMessage.INVALID_TWO_FACTOR_CODE,
                    "El codigo debe ser numerico de 6 digitos");
        }

        boolean valid = googleAuthenticator.authorize(twoFactorAuth.getSecretKey(), codeInt);
        if (!valid) {
            throw new BusinessException(DomainErrorMessage.INVALID_TWO_FACTOR_CODE,
                    "Codigo invalido. Asegurese de que su aplicacion este sincronizada");
        }

        // Generar codigos de respaldo
        String[] backupCodes = generateBackupCodes();
        String hashedBackupCodes = Arrays.stream(backupCodes)
                .map(passwordEncoder::encode)
                .collect(Collectors.joining(","));

        // Activar 2FA
        twoFactorAuth.setEnabled(true);
        twoFactorAuth.setVerified(true);
        twoFactorAuth.setEnabledAt(LocalDateTime.now());
        twoFactorAuth.setBackupCodes(hashedBackupCodes);
        twoFactorAuth.setFailedAttempts(0);
        writeRepository.save(twoFactorAuth);

        log.info("2FA habilitado para usuario: {}", userId);

        return backupCodes;
    }

    @Override
    public boolean verifyCode(UUID userId, String code) {
        TwoFactorAuth twoFactorAuth = readRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(DomainErrorMessage.TWO_FACTOR_NOT_CONFIGURED,
                        "2FA no configurado para este usuario"));

        if (!twoFactorAuth.isEnabled()) {
            return true; // Si no esta habilitado, se permite el acceso
        }

        // Verificar si esta bloqueado
        if (twoFactorAuth.getLockedUntil() != null &&
            LocalDateTime.now().isBefore(twoFactorAuth.getLockedUntil())) {
            throw new BusinessException(DomainErrorMessage.TWO_FACTOR_LOCKED,
                    "Cuenta bloqueada por demasiados intentos. Intente en " + LOCKOUT_MINUTES + " minutos");
        }

        int codeInt;
        try {
            codeInt = Integer.parseInt(code);
        } catch (NumberFormatException e) {
            recordFailedAttempt(twoFactorAuth);
            return false;
        }

        boolean valid = googleAuthenticator.authorize(twoFactorAuth.getSecretKey(), codeInt);

        if (!valid) {
            recordFailedAttempt(twoFactorAuth);
            return false;
        }

        // Resetear intentos fallidos
        if (twoFactorAuth.getFailedAttempts() > 0) {
            writeRepository.updateFailedAttempts(userId, 0, null);
        }

        return true;
    }

    @Override
    @Transactional
    public boolean verifyBackupCode(UUID userId, String backupCode) {
        TwoFactorAuth twoFactorAuth = readRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(DomainErrorMessage.TWO_FACTOR_NOT_CONFIGURED,
                        "2FA no configurado para este usuario"));

        if (twoFactorAuth.getBackupCodes() == null || twoFactorAuth.getBackupCodes().isEmpty()) {
            return false;
        }

        String[] hashedCodes = twoFactorAuth.getBackupCodes().split(",");
        List<String> remainingCodes = new ArrayList<>();
        boolean found = false;

        for (String hashedCode : hashedCodes) {
            if (!found && passwordEncoder.matches(backupCode, hashedCode)) {
                found = true;
                log.info("Codigo de respaldo usado para usuario: {}", userId);
            } else {
                remainingCodes.add(hashedCode);
            }
        }

        if (found) {
            twoFactorAuth.setBackupCodes(String.join(",", remainingCodes));
            twoFactorAuth.setFailedAttempts(0);
            twoFactorAuth.setLockedUntil(null);
            writeRepository.save(twoFactorAuth);
        }

        return found;
    }

    @Override
    @Transactional
    public void disableTwoFactor(UUID userId, String code) {
        TwoFactorAuth twoFactorAuth = readRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(DomainErrorMessage.TWO_FACTOR_NOT_CONFIGURED,
                        "2FA no configurado para este usuario"));

        if (!twoFactorAuth.isEnabled()) {
            throw new BusinessException(DomainErrorMessage.TWO_FACTOR_NOT_ENABLED,
                    "2FA no esta activado");
        }

        // Verificar codigo antes de desactivar
        if (!verifyCode(userId, code)) {
            throw new BusinessException(DomainErrorMessage.INVALID_TWO_FACTOR_CODE,
                    "Codigo invalido");
        }

        writeRepository.deleteByUserId(userId);
        log.info("2FA deshabilitado para usuario: {}", userId);
    }

    @Override
    public boolean isTwoFactorEnabled(UUID userId) {
        return readRepository.existsByUserIdAndEnabled(userId, true);
    }

    @Override
    public Optional<TwoFactorAuthDto> getTwoFactorStatus(UUID userId) {
        return readRepository.findByUserId(userId)
                .map(TwoFactorAuth::toAggregate);
    }

    @Override
    @Transactional
    public String[] regenerateBackupCodes(UUID userId, String code) {
        TwoFactorAuth twoFactorAuth = readRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(DomainErrorMessage.TWO_FACTOR_NOT_CONFIGURED,
                        "2FA no configurado para este usuario"));

        if (!twoFactorAuth.isEnabled()) {
            throw new BusinessException(DomainErrorMessage.TWO_FACTOR_NOT_ENABLED,
                    "2FA no esta activado");
        }

        // Verificar codigo
        if (!verifyCode(userId, code)) {
            throw new BusinessException(DomainErrorMessage.INVALID_TWO_FACTOR_CODE,
                    "Codigo invalido");
        }

        // Generar nuevos codigos
        String[] newCodes = generateBackupCodes();
        String hashedBackupCodes = Arrays.stream(newCodes)
                .map(passwordEncoder::encode)
                .collect(Collectors.joining(","));

        twoFactorAuth.setBackupCodes(hashedBackupCodes);
        writeRepository.save(twoFactorAuth);

        log.info("Codigos de respaldo regenerados para usuario: {}", userId);

        return newCodes;
    }

    private String[] generateBackupCodes() {
        String[] codes = new String[BACKUP_CODES_COUNT];
        for (int i = 0; i < BACKUP_CODES_COUNT; i++) {
            codes[i] = String.format("%08d", secureRandom.nextInt(100000000));
        }
        return codes;
    }

    private String generateQRCodeBase64(String otpAuthUrl) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthUrl, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | IOException e) {
            log.error("Error generando QR code: {}", e.getMessage());
            throw new BusinessException(DomainErrorMessage.QR_CODE_GENERATION_ERROR,
                    "Error generando codigo QR");
        }
    }

    private void recordFailedAttempt(TwoFactorAuth twoFactorAuth) {
        int attempts = twoFactorAuth.getFailedAttempts() + 1;
        LocalDateTime lockedUntil = null;

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            lockedUntil = LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES);
            log.warn("Usuario {} bloqueado por {} minutos por demasiados intentos de 2FA",
                    twoFactorAuth.getUserId(), LOCKOUT_MINUTES);
        }

        writeRepository.updateFailedAttempts(twoFactorAuth.getUserId(), attempts, lockedUntil);
    }

    @Override
    public PaginatedResponse search(Pageable pageable, List<FilterCriteria> filter) {
        GenericSpecificationsBuilder<TwoFactorAuth> specifications = new GenericSpecificationsBuilder<>(filter);
        Page<TwoFactorAuth> page = readRepository.findAll(specifications, pageable);

        return new PaginatedResponse(
                page.getContent().stream()
                        .map(entity -> {
                            TwoFactorAuthDto dto = entity.toAggregate();
                            String userEmail = null;
                            String userName = null;
                            try {
                                UserSystemDto user = userSystemService.findById(dto.getUserId());
                                if (user != null) {
                                    userEmail = user.getEmail();
                                    userName = (user.getName() != null ? user.getName() : "") +
                                               (user.getLastName() != null ? " " + user.getLastName() : "");
                                    userName = userName.trim();
                                }
                            } catch (Exception e) {
                                log.warn("No se pudo obtener info del usuario {}: {}", dto.getUserId(), e.getMessage());
                            }
                            return new TwoFactorAuthListResponse(dto, userEmail, userName);
                        })
                        .collect(Collectors.toList()),
                page.getTotalPages(),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getSize(),
                page.getNumber()
        );
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        TwoFactorAuth twoFactorAuth = readRepository.findById(id)
                .orElseThrow(() -> new BusinessException(DomainErrorMessage.TWO_FACTOR_NOT_CONFIGURED,
                        "Registro de 2FA no encontrado"));

        writeRepository.delete(twoFactorAuth);
        log.info("Registro de 2FA eliminado por admin. ID: {}, UserId: {}", id, twoFactorAuth.getUserId());
    }

    @Override
    public Optional<TwoFactorAuthDto> getById(UUID id) {
        return readRepository.findById(id)
                .map(TwoFactorAuth::toAggregate);
    }
}
