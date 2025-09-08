package com.bgdl.bgdl.models.dto.auth;

import com.bgdl.bgdl.enums.Role;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicUserDTO {
    private UUID id;
    private String name;
    private Double score;
    private String email;
    private Role role;
}
