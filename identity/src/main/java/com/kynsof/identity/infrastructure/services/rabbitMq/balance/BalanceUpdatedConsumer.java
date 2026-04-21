package com.kynsof.identity.infrastructure.services.rabbitMq.balance;

import com.kynsof.identity.domain.dto.BusinessDto;
import com.kynsof.identity.domain.interfaces.service.IBusinessService;
import com.kynsof.identity.infrastructure.config.IdentityCacheConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumidor de eventos de actualización de balance desde payment.
 * Sincroniza el balance del Business en identity con el balance en payment.
 */
@Service
public class BalanceUpdatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(BalanceUpdatedConsumer.class);

    private final IBusinessService businessService;
    private final CacheManager cacheManager;

    public BalanceUpdatedConsumer(IBusinessService businessService, CacheManager cacheManager) {
        this.businessService = businessService;
        this.cacheManager = cacheManager;
    }

    @Transactional
    @RabbitListener(
            queues = RabbitMQBalanceConsumerConfig.BALANCE_QUEUE,
            containerFactory = "balanceListenerContainerFactory"
    )
    public void handleBalanceUpdated(BalanceUpdatedEventDto event) {
        // Validar businessId antes de procesar para evitar errores transaccionales
        if (event.getBusinessId() == null) {
            log.warn("Evento de balance recibido con businessId=null, ignorando. operationType={}, amount={}",
                    event.getOperationType(), event.getAmount());
            return;
        }

        try {
            log.info("Recibido evento de balance: businessId={}, operationType={}, amount={}, newBalance={}",
                    event.getBusinessId(),
                    event.getOperationType(),
                    event.getAmount(),
                    event.getNewBalance());

            // Buscar el business
            BusinessDto business = businessService.findById(event.getBusinessId());

            // Actualizar el balance con el nuevo valor
            double newBalance = event.getNewBalance() != null ? event.getNewBalance().doubleValue() : 0.0;
            business.setBalance(newBalance);

            // Guardar cambios
            businessService.update(business);

            // Limpiar caché de UserMe para que los usuarios vean el balance actualizado
            clearUserMeCache();

            log.info("Balance actualizado en identity para businessId={}: newBalance={}",
                    event.getBusinessId(), newBalance);

        } catch (Exception e) {
            log.error("Error procesando evento de balance para businessId={}: {}",
                    event.getBusinessId(), e.getMessage());
            // No relanzamos la excepción para evitar reintentos infinitos
            // El balance se sincronizará en la próxima operación
        }
    }

    /**
     * Limpia todo el caché de UserMe.
     */
    private void clearUserMeCache() {
        try {
            var cache = cacheManager.getCache(IdentityCacheConfig.USER_INFO_CACHE);
            if (cache != null) {
                cache.clear();
                log.info("Caché UserMe limpiado completamente");
            }
        } catch (Exception e) {
            log.warn("Error al limpiar caché de UserMe: {}", e.getMessage());
        }
    }
}
