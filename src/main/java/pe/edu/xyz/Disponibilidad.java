package pe.edu.xyz;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.json.Json;

@Path("/libros") @Produces(MediaType.APPLICATION_JSON)
public class Disponibilidad {
    @Inject Biblioteca biblioteca;
    @GET @Path("/{id}/disponibilidad")
    public Response consultar(@PathParam("id") long id) {
        Libro l=biblioteca.libro(id);
        if (l==null) return Response.status(404).build();
        var datos=Json.createObjectBuilder().add("id",id).add("isbn",l.isbn)
            .add("titulo",l.titulo).add("ejemplares",l.ejemplares)
            .add("disponibles",l.ejemplares-biblioteca.ocupados(id)).build();
        return Response.ok(datos).header("Cache-Control","no-store").build();
    }
}
