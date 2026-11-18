package pe.edu.xyz;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.*;

@FacesValidator("isbn")
public class Isbn implements Validator<String> {
    public void validate(FacesContext contexto, UIComponent campo, String valor) {
        if (valor==null) return;
        String s = valor.trim();
        boolean valido = s.matches("[0-9]{9}[0-9X]|97[89][0-9]{10}");
        if (valido) {
            int suma=0;
            for (int i=0;i<s.length();i++) {
                int digito=s.charAt(i)=='X'?10:Character.digit(s.charAt(i),10);
                int peso=s.length()==10?10-i:(i%2==0?1:3);
                suma+=digito*peso;
            }
            valido=suma%(s.length()==10?11:10)==0;
        }
        if (!valido) throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR,"Ingrese un ISBN-10 o ISBN-13 válido sin guiones.",null));
    }
}
