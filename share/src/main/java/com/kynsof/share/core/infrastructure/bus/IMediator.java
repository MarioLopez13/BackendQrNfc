package com.kynsof.share.core.infrastructure.bus;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import com.kynsof.share.core.domain.bus.query.IQuery;
import com.kynsof.share.core.domain.bus.query.IResponse;

/**
 * Interfaz que define el patrón Mediator para el manejo de comandos y consultas
 */
public interface IMediator {
    /**
     * Envía un comando 
     * @param command Comando a ejecutar
     * @return Mensaje de respuesta del comando
     * @param <M> Tipo de mensaje de respuesta
     */
    <M extends ICommandMessage> M send(ICommand command);

    /**
     * Envía una consulta
     * @param query Consulta a ejecutar
     * @return Respuesta de la consulta
     * @param <R> Tipo de respuesta
     */
    <R extends IResponse> R send(IQuery query);
}
