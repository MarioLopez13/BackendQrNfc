package com.kynsof.identity.infrastructure.entities;

import com.kynsof.identity.domain.dto.GeographicLocationDto;
import com.kynsof.identity.domain.dto.enumType.GeographicLocationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "geographic_location", schema = "identity")
@Getter
@Setter
public class GeographicLocation implements Serializable {

    @Id
    @Column
    private UUID id;

    @Column(unique = false, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private GeographicLocationType type;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_pk_geographic_location", nullable = true)
    private GeographicLocation parent;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public GeographicLocation() {
    }

    public GeographicLocation(GeographicLocationDto geographiclocation) {
        this.id = geographiclocation.getId();
        this.name = geographiclocation.getName();
        this.type = geographiclocation.getType();
        this.parent = geographiclocation.getParent() != null
                ? new GeographicLocation(geographiclocation.getParent())
                : null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

//   public GeographicLocationDto toAggregate() {
//        GeographicLocationDto parentDto = null;
//        if (this.parent != null) {
//            parentDto = new GeographicLocationDto(this.parent.getId(), this.parent.getName(), this.parent.getType(), null);
//        }
//        return new GeographicLocationDto(this.id, this.name, this.type, parentDto);
//    }

    public GeographicLocationDto toAggregate() {
        GeographicLocationDto provinceDto = null;
        GeographicLocationDto cantonDto = null;
        GeographicLocationDto parroquiaDto = null;

        if (this.parent != null) {
            if (this.parent.getParent() != null) {
                if (this.parent.getParent().getParent() != null) {
                    provinceDto = new GeographicLocationDto(
                            this.parent.getParent().getParent().getId(),
                            this.parent.getParent().getParent().getName(),
                            this.parent.getParent().getParent().getType(),
                            null
                    );
                }
                cantonDto = new GeographicLocationDto(
                        this.parent.getParent().getId(),
                        this.parent.getParent().getName(),
                        this.parent.getParent().getType(),
                        provinceDto
                );
            }
            parroquiaDto = new GeographicLocationDto(
                    this.parent.getId(),
                    this.parent.getName(),
                    this.parent.getType(),
                    cantonDto
            );
        }

        return new GeographicLocationDto(this.id, this.name, this.type, parroquiaDto);
    }

    /**
     * Convierte a DTO sin cargar la jerarquía de padres (evita LazyInitializationException).
     * Usar cuando solo se necesitan los datos básicos de la ubicación.
     */
    public GeographicLocationDto toAggregateSimple() {
        return new GeographicLocationDto(this.id, this.name, this.type, null);
    }

}
