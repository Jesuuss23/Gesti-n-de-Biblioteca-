package pe.edu.xyz;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.*;

@FacesValidator("vencimiento")
public class Vencimiento implements Validator<java.time.LocalDate> {
    public void validate(FacesContext contexto, UIComponent campo, java.time.LocalDate valor) {
        if (valor==null) return;
        boolean valido=valor.isAfter(Biblioteca.hoy());
        if (!valido) throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,"Seleccione una fecha posterior a hoy.",null));
    }
}
