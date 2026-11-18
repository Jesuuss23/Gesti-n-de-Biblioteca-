package pe.edu.xyz;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "prestamos")

public class Prestamo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    Usuario usuario;
    @ManyToOne(optional = false)
    @JoinColumn(name = "libro_id")
    Libro libro;
    LocalDate inicio;
    LocalDate vence;
    LocalDate devuelto;
    LocalDate avisado;

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Libro getLibro() { return libro; }
    public LocalDate getInicio() { return inicio; }
    public LocalDate getVence() { return vence; }
    public LocalDate getDevuelto() { return devuelto; }
    public LocalDate getAvisado() { return avisado; }
}
