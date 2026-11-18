package pe.edu.xyz;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "usuarios")

public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String uid;
    String correo;
    @OneToMany(mappedBy = "usuario")
    List<Prestamo> prestamos;

    public Long getId() { return id; }
    public String getUid() { return uid; }
    public String getCorreo() { return correo; }
}
