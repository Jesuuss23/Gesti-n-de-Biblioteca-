package pe.edu.xyz;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Named @SessionScoped
public class Sesion implements Serializable {
    @Inject private Directorio directorio;
    @Inject private Biblioteca biblioteca;
    @Inject private OpcionesPrestamo opcionesPrestamo;
    @Inject private Reportes reportes;
    private String uid;
    private boolean personal;
    public boolean isConectado() { return uid!=null; }
    public boolean isPersonal() { return personal; }
    public String getUid() { return uid; }
    private FacesContext ctx() { return FacesContext.getCurrentInstance(); }
    private void mensaje(String s) { ctx().addMessage(null,new FacesMessage(s)); }
    public String entrar(String codigo,String clave) {
        try {
            var c=directorio.autenticar(codigo,clave);
            ((HttpServletRequest)ctx().getExternalContext().getRequest()).changeSessionId();
            uid=c.uid(); personal=c.personal();
            return "index?faces-redirect=true";
        } catch (javax.naming.NamingException | IllegalArgumentException e) {
            mensaje("Acceso denegado. Compruebe su cuenta y la conexión con el directorio.");
            return null;
        }
    }
    public String salir() {
        ctx().getExternalContext().invalidateSession();
        return "index?faces-redirect=true";
    }
    public List<Libro> getCatalogo() { return isConectado()?biblioteca.catalogo():List.of(); }
    public List<Prestamo> getActivos() { return isConectado()?biblioteca.activos(personal?null:uid):List.of(); }
    public void registrar(String isbn,String titulo,String autor,int cantidad) {
        if (!personal) return;
        try { biblioteca.registrar(isbn,titulo,autor,cantidad); opcionesPrestamo.actualizarStock(); mensaje("Libro registrado."); }
        catch (Regla e) { mensaje(e.getMessage()); }
        catch (jakarta.ejb.EJBException e) { mensaje("No se pudo guardar. Compruebe si el ISBN ya existe."); }
    }
    public void prestar(String estudiante,long id,LocalDate vence) {
        if (!personal) return;
        try { biblioteca.prestar(estudiante,id,vence); opcionesPrestamo.actualizarStock(); mensaje("Préstamo registrado."); }
        catch (Regla e) { mensaje(e.getMessage()); }
        catch (javax.naming.NamingException e) { mensaje("No se pudo validar al estudiante en LDAP."); }
    }
    public void devolver(long id) {
        if (!personal) return;
        try { biblioteca.devolver(id); opcionesPrestamo.actualizarStock(); mensaje("Devolución registrada."); }
        catch (Regla e) { mensaje(e.getMessage()); }
    }
    public void descargar() throws java.io.IOException {
        if (!personal) return;
        byte[] datos=reportes.generar();
        var respuesta=ctx().getExternalContext();
        respuesta.responseReset();
        respuesta.setResponseContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        respuesta.setResponseHeader("Content-Disposition","attachment; filename=biblioteca.xlsx");
        respuesta.getResponseOutputStream().write(datos);
        ctx().responseComplete();
    }
}
