package com.yasarbilgi.visitormeetingmanagment.room.entity;

import com.yasarbilgi.visitormeetingmanagment.common.base.TenantBaseEntity;
import com.yasarbilgi.visitormeetingmanagment.common.exception.BusinessException;
import com.yasarbilgi.visitormeetingmanagment.common.exception.ErrorCode;
import com.yasarbilgi.visitormeetingmanagment.feature.entity.Feature;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;

import java.util.HashSet;
import java.util.Set;

@Getter
@SuperBuilder
@Entity
@Filter(name = "tenantFilter")
@Table(
        name = "rooms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_rooms_company_name", columnNames = {"company_id", "name"})
        },
        indexes = {
                @Index(name = "idx_rooms_company_id", columnList = "company_id"),
                @Index(name = "idx_rooms_active", columnList = "active")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends TenantBaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "location", length = 150)
    private String location;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "description", length = 1000)
    private String description;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "room_features",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "feature_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_room_features_room_feature",
                    columnNames = {"room_id", "feature_id"}
            )
    )
    private Set<Feature> features = new HashSet<>();

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new BusinessException(ErrorCode.MEETING_ROOM_NAME_REQUIRED);
        }
        this.name = newName;
    }

    public void updateLocation(String location) {
        this.location = location;
    }

    public void updateCapacity(int newCapacity) {
        if (newCapacity < 1) {
            throw new BusinessException(ErrorCode.INVALID_ROOM_CAPACITY);
        }
        this.capacity = newCapacity;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void addFeature(Feature feature) {
        this.features.add(feature);
    }

    public void removeFeature(Feature feature) {
        this.features.remove(feature);
    }

    public boolean hasFeature(Feature feature) {
        return this.features.contains(feature);
    }

    public void deactivateIfAllowed() {
        deactivate();
    }
}