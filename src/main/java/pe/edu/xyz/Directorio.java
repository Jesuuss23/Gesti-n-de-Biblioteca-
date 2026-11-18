package pe.edu.xyz;

import jakarta.enterprise.context.ApplicationScoped;
import javax.naming.*;
import javax.naming.directory.*;
import java.util.Hashtable;
import java.util.regex.Pattern;

@ApplicationScoped
public class Directorio {
    public record Cuenta(String uid, String correo, boolean personal) {}
    private static final Pattern UID = Pattern.compile("[a-zA-Z0-9._-]{1,64}");
    private String dn(String uid) {
        if (uid == null || !UID.matcher(uid).matches()) throw new IllegalArgumentException("Código de usuario inválido.");
        return "uid=" + uid + "," + System.getenv("LDAP_PEOPLE");
    }
    private DirContext conectar(String usuario, String clave) throws NamingException {
        if (clave == null || clave.isBlank()) throw new AuthenticationException();
        var env = new Hashtable<String,String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, System.getenv("LDAP_URL"));
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, usuario);
        env.put(Context.SECURITY_CREDENTIALS, clave);
        env.put("com.sun.jndi.ldap.connect.timeout", "4000");
        env.put("com.sun.jndi.ldap.read.timeout", "4000");
        return new InitialDirContext(env);
    }
    private Cuenta consultar(DirContext ctx, String uid) throws NamingException {
        Attributes a = ctx.getAttributes(dn(uid), new String[]{"employeeType","mail","uid"});
        Attribute tipo = a.get("employeeType");
        boolean personal = tipo != null && tipo.contains("bibliotecario");
        if (!personal && (tipo == null || !tipo.contains("estudiante-activo")))
            throw new IllegalArgumentException("La cuenta no está habilitada en la universidad.");
        return new Cuenta(a.get("uid").get().toString(), a.get("mail").get().toString(), personal);
    }
    public Cuenta autenticar(String uid, String clave) throws NamingException {
        DirContext ctx = conectar(dn(uid), clave);
        try { return consultar(ctx, uid); } finally { ctx.close(); }
    }
    public java.util.SortedMap<String,String> alumnosActivos() throws NamingException {
        var alumnos = new java.util.TreeMap<String,String>();
        DirContext ctx = conectar(System.getenv("LDAP_BIND_DN"), System.getenv("LDAP_BIND_PASSWORD"));
        try {
            var controles = new SearchControls();
            controles.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            controles.setReturningAttributes(new String[]{"uid","cn"});
            var resultados = ctx.search(System.getenv("LDAP_PEOPLE"),
                "(&(employeeType=estudiante-activo)(!(employeeType=bibliotecario))(uid=*)(mail=*))", controles);
            try {
                while (resultados.hasMore()) {
                    var atributos = resultados.next().getAttributes();
                    String uid = atributos.get("uid").get().toString();
                    if (UID.matcher(uid).matches()) alumnos.put(uid, atributos.get("cn").get().toString());
                }
            } catch (SizeLimitExceededException limite) {
                // Las sugerencias son opcionales. También se admite escribir cualquier código activo.
            } finally { resultados.close(); }
        } finally { ctx.close(); }
        return alumnos;
    }
    public Cuenta estudiante(String uid) throws NamingException {
        DirContext ctx = conectar(System.getenv("LDAP_BIND_DN"), System.getenv("LDAP_BIND_PASSWORD"));
        try {
            Cuenta c = consultar(ctx, uid);
            if (c.personal()) throw new IllegalArgumentException("Seleccione una cuenta de estudiante.");
            return c;
        } finally { ctx.close(); }
    }
}
