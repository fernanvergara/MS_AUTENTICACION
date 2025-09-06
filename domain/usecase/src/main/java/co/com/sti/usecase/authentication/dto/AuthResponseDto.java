package co.com.sti.usecase.authentication.dto;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto{
    private String firstName;
    private String lastName;
    private String email;
    private String role; // El nombre del rol
    private String token;
}
