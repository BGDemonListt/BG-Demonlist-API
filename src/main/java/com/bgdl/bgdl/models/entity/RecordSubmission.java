package com.bgdl.bgdl.models.entity;

import com.bgdl.bgdl.enums.RecordStatus;
import com.bgdl.bgdl.models.baseEntity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
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
@Table(name = "records")
public class RecordSubmission extends BaseEntity {
    @NotNull
    @Min(value = 0, message = "Progress must be between 0-100")
    @Max(value = 100, message = "Progress must be between 0-100")
    private int progress;

    @NotNull
    @NotBlank(message = "Youtube URL is required")
    private String youtubeUrl;

    @NotNull
    @NotBlank(message = "Raw footage URL is required")
    private String rawFootageUrl;

    @NotNull
    @NotBlank(message = "Description is required")
    private String description;

    @ManyToOne
    @JoinColumn(name = "demon_id")
    @NotNull(message = "Demon is required")
    private Demon demon;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RecordStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull(message = "Demon is required")
    private User holder;
}
