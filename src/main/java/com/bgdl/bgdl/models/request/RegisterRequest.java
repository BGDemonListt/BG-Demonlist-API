package com.bgdl.bgdl.models.request;


import com.bgdl.bgdl.enums.Provider;
import com.bgdl.bgdl.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    private String name;
    private Role role = Role.USER;
    private Provider provider = Provider.LOCAL;
}
