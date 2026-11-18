package pe.edu.xyz;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.*;
import java.time.*;
import java.util.List;
import javax.naming.NamingException;

@Stateless
public class Biblioteca {
    @PersistenceContext EntityManager em;
    @Inject Directorio directorio;
    public static LocalDate hoy() { return LocalDate.now(ZoneId.of("America/Lima")); }
    public List<Libro> catalogo() {
        return em.createQuery("select l from Libro l order by l.titulo",Libro.class).getResultList();
    }
    public Libro libro(long id) { return em.find(Libro.class,id); }
    public long ocupados(long id) {
        return em.createQuery("select count(p) from Prestamo p where p.libro.id=:id and p.devuelto is null",Long.class)
            .setParameter("id",id).getSingleResult();
    }
    public void registrar(String isbn,String titulo,String autor,int ejemplares) {
        Libro l = new Libro();
        l.isbn=isbn.trim(); l.titulo=titulo.trim(); l.autor=autor.trim(); l.ejemplares=ejemplares;
        if (l.titulo.isEmpty() || l.autor.isEmpty() || ejemplares<1) throw new Regla("Revise los datos del libro.");
        em.persist(l);
    }
    public void prestar(String uid,long libroId,LocalDate vence) throws NamingException {
        Directorio.Cuenta cuenta;
        try { cuenta=directorio.estudiante(uid); } catch (IllegalArgumentException e) { throw new Regla(e.getMessage()); }
        if (vence==null || !vence.isAfter(hoy())) throw new Regla("El vencimiento debe ser posterior a hoy.");
        // El índice único serializa incluso dos primeros préstamos del mismo estudiante.
        em.createNativeQuery("insert into usuarios(uid,correo) values (?1,?2) on duplicate key update correo=?2")
            .setParameter(1,cuenta.uid()).setParameter(2,cuenta.correo()).executeUpdate();
        Usuario u=em.createQuery("select u from Usuario u where u.uid=:uid",Usuario.class)
            .setParameter("uid",cuenta.uid()).setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult();
        Libro l=em.find(Libro.class,libroId,LockModeType.PESSIMISTIC_WRITE);
        if (l==null || ocupados(libroId)>=l.ejemplares) throw new Regla("No quedan ejemplares disponibles.");
        long abiertos=em.createQuery("select count(p) from Prestamo p where p.usuario=:u and p.devuelto is null",Long.class)
            .setParameter("u",u).getSingleResult();
        if (abiertos>=3) throw new Regla("El estudiante ya tiene tres préstamos activos.");
        Prestamo p=new Prestamo();
        p.usuario=u; p.libro=l; p.inicio=hoy(); p.vence=vence;
        em.persist(p);
    }
    public List<Prestamo> activos(String uid) {
        String jpql="select p from Prestamo p join fetch p.usuario join fetch p.libro where p.devuelto is null";
        var q=em.createQuery(jpql+(uid==null?"":" and p.usuario.uid=:uid")+" order by p.vence,p.id",Prestamo.class);
        if (uid!=null) q.setParameter("uid",uid);
        return q.getResultList();
    }
    public void devolver(long id) {
        Prestamo p=em.find(Prestamo.class,id,LockModeType.PESSIMISTIC_WRITE);
        if (p==null || p.devuelto!=null) throw new Regla("El préstamo ya está cerrado.");
        p.devuelto=hoy();
    }
}
