-- Datos ficticios para la demostración local.
SET NAMES utf8mb4;
START TRANSACTION;
INSERT INTO libros (isbn, titulo, autor, ejemplares) VALUES
('9780007000012', 'Programación en Java', 'Irene Campos', 2),
('9780007000029', 'Bases de datos relacionales', 'Jorge Prado', 3),
('9780007000036', 'Algoritmos y estructuras de datos', 'Rosa Linares', 4),
('9780007000043', 'Redes de computadoras', 'Mario Vidal', 5),
('9780007000050', 'Diseño de interfaces accesibles', 'Elena Bravo', 6),
('9780007000067', 'Ingeniería de software', 'Manuel Rivas', 2),
('9780007000074', 'Estadística aplicada', 'Irene Campos', 3),
('9780007000081', 'Matemática discreta', 'Jorge Prado', 4),
('9780007000098', 'Arquitectura de computadoras', 'Rosa Linares', 5),
('9780007000104', 'Sistemas operativos', 'Mario Vidal', 6),
('9780007000111', 'Metodología de la investigación', 'Elena Bravo', 2),
('9780007000128', 'Desarrollo web con Jakarta EE', 'Manuel Rivas', 3)
ON DUPLICATE KEY UPDATE id = libros.id;

INSERT INTO usuarios (uid, correo) VALUES
('alumno01', 'alumno01@xyz.test'),
('alumno02', 'alumno02@xyz.test'),
('alumno03', 'alumno03@xyz.test'),
('alumno04', 'alumno04@xyz.test'),
('alumno05', 'alumno05@xyz.test'),
('alumno06', 'alumno06@xyz.test'),
('alumno07', 'alumno07@xyz.test'),
('alumno08', 'alumno08@xyz.test'),
('alumno09', 'alumno09@xyz.test'),
('alumno10', 'alumno10@xyz.test')
ON DUPLICATE KEY UPDATE id = usuarios.id;
COMMIT;
