package com.kynsof.identity.infrastructure.entities;

import com.kynsof.identity.domain.dto.AdvertisementImageDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(
        name = "advertisement_image",
        schema = "identity",
        indexes = {
                @Index(name = "idx_advertisement_business", columnList = "business_id"),
                @Index(name = "idx_advertisement_key", columnList = "imagen_key")
        }
)
public class AdvertisementImage {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "imagen_key", nullable = false, length = 500)
    private String imagenKey;

    @Column(name = "position", length = 100)
    private String position;

    @Column(name = "business_id", nullable = false)
    private UUID businessId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    private Business business;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public AdvertisementImage(AdvertisementImageDto dto) {
        this.id = dto.getId();
        this.imagenKey = dto.getImagenKey();
        this.position = dto.getPosition();
        this.businessId = dto.getBusinessId();
    }

    public AdvertisementImageDto toAggregate() {
        return new AdvertisementImageDto(
                this.id,
                this.imagenKey,
                this.position,
                this.businessId
        );
    }
}
