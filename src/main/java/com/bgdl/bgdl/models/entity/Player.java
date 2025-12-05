package com.bgdl.bgdl.models.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

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

    @NotNull
    private Double points = 0.0;

    private Integer rank = null;

    private boolean banned = false;

    @ManyToOne
    @JoinColumn(name = "hardest_demon_id")
    private Demon hardestDemon;

    @OneToOne(mappedBy = "player")
    private User user;

    @ManyToMany
    private List<Demon> completedDemons;
}
