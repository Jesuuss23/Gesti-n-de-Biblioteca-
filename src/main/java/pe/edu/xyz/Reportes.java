package pe.edu.xyz;

import jakarta.ejb.*;
import jakarta.persistence.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Stateless
public class Reportes {
    @PersistenceContext EntityManager em;
    public byte[] generar() throws IOException {
        try(var libro=new XSSFWorkbook(); var out=new ByteArrayOutputStream()) {
            hoja(libro,"Más prestados","ISBN","Título","Préstamos",em.createQuery(
              "select p.libro.isbn,p.libro.titulo,count(p) from Prestamo p group by p.libro.id,p.libro.isbn,p.libro.titulo order by count(p) desc,p.libro.isbn",Object[].class).getResultList());
            hoja(libro,"Morosidad","Usuario","Correo","Vencidos",em.createQuery(
              "select p.usuario.uid,p.usuario.correo,count(p) from Prestamo p where p.devuelto is null and p.vence<:hoy group by p.usuario.id,p.usuario.uid,p.usuario.correo order by count(p) desc,p.usuario.uid",Object[].class)
              .setParameter("hoy",Biblioteca.hoy()).getResultList());
            libro.write(out);
            return out.toByteArray();
        }
    }
    private void hoja(XSSFWorkbook libro,String nombre,String a,String b,String c,List<Object[]> datos) {
        var hoja=libro.createSheet(nombre);
        var cab=hoja.createRow(0);
        cab.createCell(0).setCellValue(a); cab.createCell(1).setCellValue(b); cab.createCell(2).setCellValue(c);
        int i=1;
        for (Object[] fila:datos) {
            var r=hoja.createRow(i++);
            r.createCell(0).setCellValue(fila[0].toString());
            r.createCell(1).setCellValue(fila[1].toString());
            r.createCell(2).setCellValue(((Number)fila[2]).longValue());
        }
        hoja.setColumnWidth(0,22*256); hoja.setColumnWidth(1,50*256); hoja.setColumnWidth(2,16*256);
    }
    @Schedule(hour="6",minute="0",timezone="America/Lima",persistent=false)
    public void diario() throws IOException {
        Path carpeta=Path.of(System.getProperty("jboss.server.data.dir"),"reportes");
        Files.createDirectories(carpeta);
        Path tmp=Files.createTempFile(carpeta,"biblioteca-",".tmp");
        try {
            Files.write(tmp,generar());
            Files.move(tmp,carpeta.resolve(Biblioteca.hoy()+".xlsx"),StandardCopyOption.REPLACE_EXISTING);
        } finally { Files.deleteIfExists(tmp); }
    }
}
