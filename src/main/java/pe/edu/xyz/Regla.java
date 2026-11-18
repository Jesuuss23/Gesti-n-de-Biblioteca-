package pe.edu.xyz;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback=true)
public class Regla extends RuntimeException {
    public Regla(String mensaje) { super(mensaje); }
}
