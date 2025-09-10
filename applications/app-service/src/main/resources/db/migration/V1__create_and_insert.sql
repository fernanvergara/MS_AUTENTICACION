CREATE TABLE rol (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255)
);

INSERT INTO rol (id, nombre, descripcion)
VALUES (1, 'ADMIN', 'Tiene control total sobre el sistema.'),
       (2, 'ASESOR', 'Puede gestionar clientes y solicitudes.'),
       (3, 'CLIENTE', 'Puede realizar solicitudes y consultar su historial.');


CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL UNIQUE,
    documento_identidad VARCHAR(15) NOT NULL UNIQUE,
    fecha_nacimiento DATE NOT NULL,
    telefono VARCHAR(20),
    direccion VARCHAR(255)
    id_rol BIGINT,
    salario_base NUMERIC(10, 2)
);
