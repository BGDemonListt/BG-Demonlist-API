package com.bgdl.bgdl.models.entity;

import com.bgdl.bgdl.models.baseEntity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "records")
public class Record extends BaseEntity {
    private String description;
}
