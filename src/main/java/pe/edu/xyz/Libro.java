package pe.edu.xyz;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "libros")
@Cacheable
public class Libro {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String isbn;
    String titulo;
    String autor;
    int ejemplares;
    @OneToMany(mappedBy = "libro")
    List<Prestamo> prestamos;

    public Long getId() { return id; }
    public String getIsbn() { return isbn; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public int getEjemplares() { return ejemplares; }
}
