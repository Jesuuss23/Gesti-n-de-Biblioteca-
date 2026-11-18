package pe.edu.xyz;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;

@Named @RequestScoped
public class OpcionesPrestamo {
    @Inject Sesion sesion;
    @Inject Directorio directorio;
    @Inject Biblioteca biblioteca;
    private List<Map.Entry<String,String>> alumnos;
    private Map<Long,Long> stock;

    public List<Map.Entry<String,String>> getAlumnos() {
        if (!sesion.isPersonal()) return List.of();
        if (alumnos == null) {
            try { alumnos = List.copyOf(directorio.alumnosActivos().entrySet()); }
            catch (NamingException e) { alumnos = List.of(); }
        }
        return alumnos;
    }
    public Map<Long,Long> getStock() {
        if (!sesion.isPersonal()) return Map.of();
        if (stock == null) stock = biblioteca.existencias();
        return stock;
    }
    public void actualizarStock() { stock = null; }
}
