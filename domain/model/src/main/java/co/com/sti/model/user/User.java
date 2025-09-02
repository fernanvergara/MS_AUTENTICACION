package co.com.sti.model.user;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    private String name;
    private String lastName;
    private String email;
    private String numberIdentity;
    private LocalDate birthDate;
    private String phoneNumber;
    private String address;
    private Long idRole;
    private BigDecimal salary;
}
