package co.com.sti.api.dto;

import co.com.sti.api.dto.validation.AgeValid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateUserDTO(
        @NotBlank(message = "El nombre no puede estar vacío")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        String name,

        @NotBlank(message = "El apellido no puede estar vacío")
        @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
        String lastName,

        @NotBlank(message = "El email no puede estar vacío")
        @Email(message = "El formato del email es inválido")
        String email,

        @NotBlank(message = "El número de identidad no puede estar vacío")
        @Pattern(regexp = "\\d+")
        String numberIdentity,

        @NotNull(message = "La fecha de nacimiento no puede estar vacía")
        @AgeValid(message = "Debes tener al menos 18 años para registrarte.")
        LocalDate birthDate,

        @NotBlank(message = "El número de teléfono no puede estar vacío")
        @Pattern(regexp = "\\d{10}")
        String phoneNumber,

        @NotBlank(message = "La dirección de contacto no puede estar vacía")
        String address,

        @NotNull(message = "El ID de rol no puede ser nulo")
        Integer idRole,

        @NotNull(message = "El salario no puede ser nulo")
        @DecimalMin(value = "0.0", message = "El salario debe ser mayor o igual a cero")
        @DecimalMax(value = "15000000.0", message = "El salario debe ser menor o igual a 1500000.0")
        BigDecimal salary) {
}
