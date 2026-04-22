package com.bgdl.bgdl.models.response;

import com.bgdl.bgdl.enums.Role;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicUserResponse {
    private UUID id;
    private String name;
    private Double points;
    private String email;
    private Role role;
}
