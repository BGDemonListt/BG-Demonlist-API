package com.bgdl.bgdl.models.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "players")
public class Player extends BaseEntity {
    @Size(min = 2, message = "The name should be at least 2 symbols!")
    private String name;

    @Builder.Default
    @NotNull
    private Double points = 0.0;

    @Builder.Default
    private Integer rank = null;

    @ManyToOne
    @JoinColumn(name = "hardest_demon_id")
    private Demon hardestDemon;

    @OneToOne(mappedBy = "player")
    private User user;

    @Builder.Default
    @ManyToMany
    @OrderBy("position ASC")
    private Set<Demon> completedDemons = new LinkedHashSet<>();
}
