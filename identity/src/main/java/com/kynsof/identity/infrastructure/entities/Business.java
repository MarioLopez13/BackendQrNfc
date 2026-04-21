package com.kynsof.identity.infrastructure.entities;

import com.kynsof.identity.domain.dto.BusinessDto;
import com.kynsof.identity.domain.dto.enumType.EBusinessStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(
        name = "business",
        schema = "identity",
        indexes = {
                @Index(name = "idx_business_ruc", columnList = "ruc"),
                @Index(name = "idx_business_email", columnList = "email"),
                @Index(name = "idx_business_status", columnList = "status"),
                @Index(name = "idx_business_responsible", columnList = "id_responsible"),
                @Index(name = "idx_business_seller", columnList = "seller")
        }
)
public class Business {
    @Id
    @Column(name = "id")
    protected UUID id;
    private String name;
    private String latitude;
    private String longitude;
    private String description;
    private String logo;
    private String ruc;
    @Column()
    private String address;
    @Enumerated(EnumType.STRING)
    private EBusinessStatus status;
    private double balance;
    private String phone;
    private String email;
    private String webSite;
    private String storageCapacity;
    @Column(name = "id_responsible")
    private UUID idResponsible;
    @Column(name = "fixed_price")
    private Double fixedPrice;
    @Column(name = "is_charged_cer_consultation")
    private Boolean isChargedPerConsultation;
    private UUID seller;
    @Column(name = "zone_id")
    private String zoneId;

    @OneToMany(mappedBy = "business")
    private Set<UserPermissionBusiness> userPermissionBusinesses = new HashSet<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "geographicLocation_id")
    private GeographicLocation geographicLocation;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Business(BusinessDto business) {
        this.id = business.getId();
        this.name = business.getName();
        this.latitude = business.getLatitude();
        this.longitude = business.getLongitude();
        this.description = business.getDescription();
        this.logo = business.getLogo();
        this.ruc = business.getRuc();
        this.status = business.getStatus();
        this.geographicLocation = business.getGeographicLocationDto() != null ? new GeographicLocation(business.getGeographicLocationDto()) : null;
        this.address = business.getAddress();
        this.balance = business.getBalance();
        this.phone = business.getPhone();
        this.email = business.getEmail();
        this.webSite = business.getWebSite();
        this.storageCapacity = business.getStorageCapacity();
        this.idResponsible = business.getIdResponsible();
        this.fixedPrice = business.getFixedPrice();
        this.isChargedPerConsultation = business.getIsChargedPerConsultation();
        this.seller = business.getSeller();
        this.zoneId = business.getZoneId();
    }

    public BusinessDto toAggregate () {
        // Usar toAggregateSimple() para GeographicLocation para evitar LazyInitializationException
        // La jerarquía de ubicación (parent) ahora es LAZY
        BusinessDto businessDto = new BusinessDto(
                id,
                name,
                latitude,
                longitude,
                description,
                logo,
                ruc,
                status,
                geographicLocation != null ? geographicLocation.toAggregateSimple() : null,
                address,
                phone,
                email,
                webSite,
                storageCapacity,
                idResponsible,
                fixedPrice,
                isChargedPerConsultation,
                seller,
                zoneId
        );
        businessDto.setBalance(balance);
        businessDto.setCreateAt(createdAt);
        return businessDto;
    }
}
