package co.com.sti.model.role;

import lombok.Getter;

import java.util.stream.Stream;

@Getter
public enum Role {
    ADMIN(1, "ADMIN", "Tiene control total sobre el sistema."),
    ADVISOR(2, "ASESOR", "Puede gestionar clientes y solicitudes."),
    CLIENT(3, "CLIENTE", "Puede realizar solicitudes y consultar su historial.");

    private final int idRole;
    private final String name;
    private final String description;

    Role(int id, String name, String description) {
        this.idRole = id;
        this.name = name;
        this.description = description;
    }

    public int getId() {
        return idRole;
    }

    public static Role getById(int id) {
        return Stream.of(Role.values())
                .filter(role -> role.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Rol no existe con ID: " + id));
    }

}
