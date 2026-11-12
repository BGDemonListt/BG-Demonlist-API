package com.bgdl.bgdl.models.entity;

import com.bgdl.bgdl.enums.BulgarianRegion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@DynamicUpdate
@Table(name = "players", indexes = {
        @Index(name = "idx_players_leaderboard_rank", columnList = "deleted_at, rank"),
        @Index(name = "idx_players_leaderboard_points", columnList = "deleted_at, points")
})
public class Player extends BaseEntity {
    @Size(min = 2, message = "Името трябва да е поне 2 символа!")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 64)
    private BulgarianRegion region;

    @Builder.Default
    @NotNull
    private Double points = 0.0;

    @Builder.Default
    private Integer rank = null;

    @ManyToOne
    @JoinColumn(name = "hardest_demon_id")
    private Demon hardestDemon;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "player_completed_demons",
            joinColumns = @JoinColumn(name = "player_id"),
            inverseJoinColumns = @JoinColumn(name = "demon_id")
    )
    @OrderBy("position ASC")
    private Set<Demon> completedDemons = new LinkedHashSet<>();
}
