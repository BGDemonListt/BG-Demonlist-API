package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.exceptions.common.NoSuchElementException;
import com.bgdl.bgdl.models.entity.BaseEntity;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public abstract class BaseService<T extends BaseEntity, ID> {

    protected abstract JpaRepository<T, ID> getRepository();
    protected abstract NoSuchElementException notFoundException();

    protected T getEntityById(ID id) {
        return getRepository()
                .findById(id)
                .orElseThrow(this::notFoundException);
    }

    protected T getEntityById(ID id, boolean deletedCheck) {
        T entity = getEntityById(id);

        if (deletedCheck && entity.getDeletedAt() != null) {
            throw this.notFoundException();
        }

        return entity;
    }

    protected void delete(ID id) {
        T entity = getEntityById(id);

        if (entity.getDeletedAt() == null) {
            entity.setDeletedAt(LocalDateTime.now());
        } else {
            entity.setDeletedAt(null); // restore
        }

        getRepository().save(entity);
    }
}