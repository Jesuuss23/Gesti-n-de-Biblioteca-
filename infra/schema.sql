CREATE TABLE libros (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, isbn VARCHAR(13) NOT NULL UNIQUE,
 titulo VARCHAR(200) NOT NULL, autor VARCHAR(160) NOT NULL, ejemplares INT NOT NULL CHECK (ejemplares > 0)
);
CREATE TABLE usuarios (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, uid VARCHAR(64) NOT NULL UNIQUE, correo VARCHAR(254) NOT NULL
);
CREATE TABLE prestamos (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, usuario_id BIGINT NOT NULL, libro_id BIGINT NOT NULL,
 inicio DATE NOT NULL, vence DATE NOT NULL, devuelto DATE, avisado DATE,
 FOREIGN KEY (usuario_id) REFERENCES usuarios(id), FOREIGN KEY (libro_id) REFERENCES libros(id),
 INDEX (usuario_id, devuelto), INDEX (libro_id, devuelto), INDEX (devuelto, vence)
);