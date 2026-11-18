package pe.edu.xyz;

import jakarta.ejb.*;
import jakarta.persistence.*;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

@Stateless
public class Correo {
    @PersistenceContext EntityManager em;
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void enviar(long id) throws MessagingException {
        var hoy=Biblioteca.hoy();
        var p=em.find(Prestamo.class,id,LockModeType.PESSIMISTIC_WRITE);
        if (p.devuelto!=null || hoy.equals(p.avisado)) return;
        Properties cfg=new Properties();
        cfg.setProperty("mail.smtp.host",System.getenv("SMTP_HOST"));
        cfg.setProperty("mail.smtp.port",System.getenv("SMTP_PORT"));
        cfg.setProperty("mail.smtp.connectiontimeout","5000");
        cfg.setProperty("mail.smtp.timeout","5000");
        var m=new MimeMessage(Session.getInstance(cfg));
        m.setFrom(new InternetAddress(System.getenv("SMTP_FROM")));
        m.setRecipient(Message.RecipientType.TO,new InternetAddress(p.usuario.correo));
        m.setSubject(p.vence.isBefore(hoy)?"Préstamo vencido":"Recordatorio de devolución","UTF-8");
        m.setText("Libro: "+p.libro.titulo+"\nFecha de devolución: "+p.vence,"UTF-8");
        Transport.send(m);
        p.avisado=hoy;
    }
}
