package com.bgdl.bgdl.models.entity;

import com.bgdl.bgdl.enums.RecordSubmissionStatus;
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
@Table(name = "record_submissions")
public class RecordSubmission extends BaseEntity {
    @NotNull
    @Min(value = 0, message = "Прогресът трябва да бъде между 0 и 100")
    @Max(value = 100, message = "Прогресът трябва да бъде между 0 и 100")
    private int progress;

    @NotNull
    @NotBlank(message = "YouTube URL адресът е задължителен")
    private String youtubeUrl;

    @NotNull
    @NotBlank(message = "URL адресът на суровия запис е задължителен")
    private String rawFootageUrl;

    @NotNull
    @NotBlank(message = "Описанието е задължително")
    private String description;

    @ManyToOne
    @JoinColumn(name = "demon_id")
    @NotNull(message = "Демонът е задължителен")
    private Demon demon;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RecordSubmissionStatus status;

    @ManyToOne
    @JoinColumn(name = "holder_id")
    @NotNull(message = "Играчът е задължителен")
    private Player holder;
}
