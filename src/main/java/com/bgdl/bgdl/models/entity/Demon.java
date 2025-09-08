package com.bgdl.bgdl.models.entity;

import com.bgdl.bgdl.enums.gd.DemonDifficulty;
import com.bgdl.bgdl.models.baseEntity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "demons")
public class Demon extends BaseEntity {
    @NotNull
    @NotBlank(message = "Level title is required")
    private String levelTitle;

    @NotNull
    @Min(value = 1, message = "Level ID is required")
    private Long levelId;

    @NotNull
    @NotBlank(message = "Creator name is required")
    private String creatorName;

    @NotNull
    @Min(value = 1, message = "Creator ID is required")
    private Long creatorId;

    @NotNull
    @NotBlank(message = "Description is required")
    private String description;

    @NotNull
    @NotBlank(message = "Level password is required")
    private String levelPassword;

    @NotNull
    @NotBlank(message = "Music name is required")
    private String musicName;

    @NotNull
    @Min(value = 1, message = "Music ID is required")
    private Long musicId;

    @NotNull
    @NotBlank(message = "Music creator is required")
    private String musicCreatorName;

    @NotNull
    @NotBlank(message = "Music URL is required")
    private String musicUrl;

    @NotNull
    private int requirement;

    @NotNull
    private int position;

    @NotNull
    private Double points;

    @NotNull
    @Enumerated(EnumType.STRING)
    private DemonDifficulty difficulty;

    public void setPosition(int position) {
        this.position = position;
        this.points = calculatePoints();
    }

    private Double calculatePoints() {
        // TODO: Add point formula
        return this.position + 1.5;
    }
}
