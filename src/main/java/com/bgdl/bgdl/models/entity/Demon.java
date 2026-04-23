package com.bgdl.bgdl.models.entity;

import com.bgdl.bgdl.enums.gd.DemonDifficulty;
import com.bgdl.bgdl.exceptions.demon.DemonInvalidPositionException;
import jakarta.persistence.*;
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
    private static final double BASE_POINTS = 1.0;
    private static final double TOP_DEMON_POINTS = 323.0;

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

    public void recalculatePoints(int totalDemons) {
        if (this.position < 1 || totalDemons < 1) {
            throw new DemonInvalidPositionException();
        }

        if (this.position == 1 || totalDemons == 1) {
            this.points = TOP_DEMON_POINTS;
            return;
        }

        double exponent = -Math.log(TOP_DEMON_POINTS - BASE_POINTS) / (totalDemons - 1) * (this.position - 1);
        this.points = BASE_POINTS + (TOP_DEMON_POINTS - BASE_POINTS) * Math.exp(exponent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Demon)) return false;
        Demon other = (Demon) o;
        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
