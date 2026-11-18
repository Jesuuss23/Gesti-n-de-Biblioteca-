package pe.edu.xyz;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import java.time.LocalDate;

@Named @RequestScoped
public class Formulario {
    private String codigo;
    private String clave;
    private String isbn;
    private String titulo;
    private String autor;
    private int cantidad = 1;
    private String estudiante;
    private Long libroId;
    private LocalDate vence = Biblioteca.hoy().plusDays(7);
    public String getCodigo() { return codigo; }
    public void setCodigo(String valor) { codigo=valor; }
    public String getClave() { return clave; }
    public void setClave(String valor) { clave=valor; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String valor) { isbn=valor; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String valor) { titulo=valor; }
    public String getAutor() { return autor; }
    public void setAutor(String valor) { autor=valor; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int valor) { cantidad=valor; }
    public String getEstudiante() { return estudiante; }
    public void setEstudiante(String valor) { estudiante=valor; }
    public Long getLibroId() { return libroId; }
    public void setLibroId(Long valor) { libroId=valor; }
    public LocalDate getVence() { return vence; }
    public void setVence(LocalDate valor) { vence=valor; }
}
