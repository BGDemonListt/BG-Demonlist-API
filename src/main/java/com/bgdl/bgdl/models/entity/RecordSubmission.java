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
@Table(name = "record_submissions", indexes = {
        @Index(name = "idx_records_holder_status", columnList = "holder_id, status, deleted_at"),
        @Index(name = "idx_records_demon_status", columnList = "demon_id, status, deleted_at")
})
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
    @Column(columnDefinition = "text")
    private String description;

    @ManyToOne
    @JoinColumn(name = "demon_id")
    @NotNull(message = "Демонът е задължителен")
    private Demon demon;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private RecordSubmissionStatus status;

    @ManyToOne
    @JoinColumn(name = "holder_id")
    @NotNull(message = "Играчът е задължителен")
    private Player holder;
}
