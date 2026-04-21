package com.kynsof.share.core.domain.bus.command;

/**
 * Interfaz para Commands que pueden ser reutilizados mediante Object Pooling
 * Optimiza el uso de memoria para commands frecuentes en todos los microservicios
 */
public interface IPoolableCommand extends ICommand {
    
    /**
     * Limpia el estado del comando para poder reutilizarlo
     * Debe resetear todos los campos a su estado inicial (null, false, 0, etc.)
     */
    void reset();
    
    /**
     * Configura el comando con nuevos datos
     * @param data Los datos para configurar el comando (puede ser Request, DTO, etc.)
     */
    void populate(Object data);
    
    /**
     * Indica si el comando está en un estado válido para ser reutilizado
     * @return true si puede ser reutilizado, false en caso contrario
     */
    default boolean isReusable() {
        return true;
    }
    
    /**
     * Obtiene el tipo de comando para identificación en el pool
     * @return El nombre de la clase del comando
     */
    default String getCommandType() {
        return this.getClass().getSimpleName();
    }
}
