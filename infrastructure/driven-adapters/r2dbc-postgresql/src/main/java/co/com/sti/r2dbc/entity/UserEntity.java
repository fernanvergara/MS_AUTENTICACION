package co.com.sti.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Table("usuario")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserEntity {
    @Id
    private Long id;

    @Column("nombre")
    private String name;

    @Column("apellido")
    private String lastName;

    @Column("email")
    private String email;

    @Column("documento_identidad")
    private String numberIdentity;

    @Column("fecha_nacimiento")
    private LocalDate birthDate;

    @Column("telefono")
    private String phoneNumber;

    @Column("direccion")
    private String address;

    @Column("id_rol")
    private Long idRole;

    @Column("salario_base")
    private BigDecimal salary;
}
