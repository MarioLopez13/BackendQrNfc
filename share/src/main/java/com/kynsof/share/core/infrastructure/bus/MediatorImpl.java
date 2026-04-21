package com.kynsof.share.core.infrastructure.bus;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import com.kynsof.share.core.domain.bus.query.IQuery;
import com.kynsof.share.core.domain.bus.query.IResponse;
import com.kynsof.share.core.infrastructure.bus.command.InMemoryCommandBus;
import com.kynsof.share.core.infrastructure.bus.query.InMemoryQueryBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Implementación del patrón Mediator para gestionar comandos y consultas
 * Activa solo cuando el cache está deshabilitado
 */
@Component
//@ConditionalOnProperty(name = "mediator.cache.enabled", havingValue = "false", matchIfMissing = true)
public class MediatorImpl implements IMediator {

    private static final Logger logger = LoggerFactory.getLogger(MediatorImpl.class);
    private final InMemoryCommandBus commandBus;
    private final InMemoryQueryBus queryBus;

    public MediatorImpl(InMemoryCommandBus commandBus, InMemoryQueryBus queryBus) {
        this.commandBus = Objects.requireNonNull(commandBus, "CommandBus no puede ser null");
        this.queryBus = Objects.requireNonNull(queryBus, "QueryBus no puede ser null");
    }

    @Override
    public <M extends ICommandMessage> M send(ICommand command) {
        try {
            Objects.requireNonNull(command, "Command no puede ser null");
            logger.debug("Ejecutando comando: {}", command.getClass().getSimpleName());
            
            commandBus.dispatch(command);
            M message = (M) command.getMessage();
            
            logger.debug("Comando ejecutado exitosamente: {}", command.getClass().getSimpleName());
            return message;
        } catch (Exception e) {
            logger.error("Error ejecutando comando {}: {}", command.getClass().getSimpleName(), e.getMessage(), e);
            throw new MediatorException("Error ejecutando comando", e);
        }
    }

    @Override
    public <R extends IResponse> R send(IQuery query) {
        try {
            Objects.requireNonNull(query, "Query no puede ser null");
            logger.debug("Ejecutando consulta: {}", query.getClass().getSimpleName());
            
            R response = (R) queryBus.ask(query);
            
            logger.debug("Consulta ejecutada exitosamente: {}", query.getClass().getSimpleName());
            return response;
        } catch (Exception e) {
            logger.error("Error ejecutando consulta {}: {}", query.getClass().getSimpleName(), e.getMessage(), e);
            throw new MediatorException("Error ejecutando consulta", e);
        }
    }
}
