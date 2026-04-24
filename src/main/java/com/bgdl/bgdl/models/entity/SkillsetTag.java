package com.bgdl.bgdl.models.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "skillset_tags")
public class SkillsetTag extends BaseEntity {
    @NotBlank(message = "Името на тага е задължително")
    @Size(max = 50, message = "Името на тага трябва да е до 50 символа")
    @Column(nullable = false, length = 50)
    private String name;
}
