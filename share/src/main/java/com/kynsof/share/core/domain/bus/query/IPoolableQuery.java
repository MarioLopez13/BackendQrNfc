package com.kynsof.share.core.domain.bus.query;

/**
 * Interfaz para Queries que pueden ser reutilizados mediante Object Pooling
 * Optimiza el uso de memoria para queries frecuentes en todos los microservicios
 */
public interface IPoolableQuery extends IQuery {
    
    /**
     * Limpia el estado del query para poder reutilizarlo
     * Debe resetear todos los campos a su estado inicial (null, false, 0, etc.)
     */
    void reset();
    
    /**
     * Configura el query con nuevos datos
     * @param data Los datos para configurar el query (puede ser parámetros, filtros, etc.)
     */
    void populate(Object data);
    
    /**
     * Indica si el query está en un estado válido para ser reutilizado
     * @return true si puede ser reutilizado, false en caso contrario
     */
    default boolean isReusable() {
        return true;
    }
    
    /**
     * Obtiene el tipo de query para identificación en el pool
     * @return El nombre de la clase del query
     */
    default String getQueryType() {
        return this.getClass().getSimpleName();
    }
}
