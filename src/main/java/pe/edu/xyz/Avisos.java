package pe.edu.xyz;

import jakarta.ejb.*;
import jakarta.inject.Inject;
import jakarta.persistence.*;
import java.util.logging.Logger;

@Stateless
public class Avisos {
    @PersistenceContext EntityManager em;
    @Inject Correo correo;
    @Schedule(hour="*",minute="*/15",persistent=false)
    public void revisar() {
        var hoy=Biblioteca.hoy();
        var ids=em.createQuery("select p.id from Prestamo p where p.devuelto is null and p.vence<=:limite and (p.avisado is null or p.avisado<:hoy)",Long.class)
            .setParameter("limite",hoy.plusDays(1)).setParameter("hoy",hoy).getResultList();
        for (Long id:ids) {
            try { correo.enviar(id); }
            catch (Exception e) { Logger.getLogger("biblioteca").warning("Aviso pendiente para préstamo "+id); }
        }
    }
}
