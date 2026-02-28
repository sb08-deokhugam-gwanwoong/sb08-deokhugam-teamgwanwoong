package com.codeit.project.sb08deokhugamteamgwanwoong.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class BaseRemovableEntity extends BaseUpdatableEntity {

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public void delete() {
        this.deletedAt = Instant.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
