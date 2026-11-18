"""Prueba HTTP de la instalación local desechable de Compose; requiere una instalación nueva con los datos de demostración."""
import datetime
import http.cookiejar
import io
import json
import subprocess
import time
import urllib.error
import urllib.parse
import urllib.request
import zipfile
from html.parser import HTMLParser
from xml.etree import ElementTree as ET
from zoneinfo import ZoneInfo

URL = "http://localhost:8080/biblioteca/"
http = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
http.addheaders = [("User-Agent", "Biblioteca-integration")]


class Formularios(HTMLParser):
    def __init__(self, html):
        super().__init__()
        self.forms = []
        self.form = None
        self.feed(html)

    def handle_starttag(self, tag, attrs):
        a = dict(attrs)
        if tag == "form":
            self.form = (a["action"], {}, {})
            self.forms.append(self.form)
        if tag == "input" and self.form and "name" in a:
            if a.get("type") == "hidden":
                self.form[1][a["name"]] = a.get("value", "")
            if a.get("type") == "submit":
                self.form[2][a["value"]] = a["name"]

    def handle_endtag(self, tag):
        if tag == "form":
            self.form = None


def pulsar(html, boton, valores=None):
    action, hidden, botones = next(f for f in Formularios(html).forms if boton in f[2])
    payload = {**hidden, **(valores or {}), botones[boton]: boton}
    return http.open(urllib.parse.urljoin(URL, action), urllib.parse.urlencode(payload).encode()).read()


def ingreso(html, uid):
    return pulsar(html, "Ingresar", {"acceso:codigo": uid, "acceso:clave": "DemoXYZ2026"}).decode()


def sql(consulta):
    return subprocess.check_output(["docker", "compose", "exec", "-T", "mysql", "sh", "-c",
        'MYSQL_PWD="$MYSQL_PASSWORD" mysql -N -B -u biblioteca biblioteca'], input=consulta.encode()).decode().strip()


def disponibilidad():
    return json.load(http.open(URL + f"api/libros/{libro_id}/disponibilidad"))["disponibles"]


for intento in range(90):
    try:
        inicio = http.open(URL, timeout=5).read().decode()
        if "Ingresar" in inicio:
            break
    except OSError:
        pass
    time.sleep(3)
else:
    raise RuntimeError("No se desplegó la aplicación")

assert "Acceso denegado" in ingreso(inicio, "inactivo")
assert sql("SELECT COUNT(*), SUM(ejemplares) FROM libros") == "12\t45"
assert sql("SELECT COUNT(*) FROM usuarios") == "10"
for i in range(1, 11):
    sesion = ingreso(http.open(URL).read().decode(), f"alumno{i:02d}")
    assert "Cerrar sesión" in sesion and "Registrar devolución" not in sesion
    assert "Ver códigos y nombres" not in sesion and "Ana Torres" not in sesion
    pulsar(sesion, "Cerrar sesión")
pagina = ingreso(http.open(URL).read().decode(), "operador")
assert "alumno01 · Ana Torres" in pagina and "alumno10 · Daniel Luna" in pagina
assert 'value="inactivo"' not in pagina and 'value="operador"' not in pagina
libro = {"registro:isbn": "9780134685990", "registro:titulo": "Java",
         "registro:autor": "Joshua Bloch", "registro:cantidad": "4"}
pagina = pulsar(pagina, "Guardar libro", libro).decode()
assert "ISBN-10" in pagina
libro["registro:isbn"] = "9780134685991"
pagina = pulsar(pagina, "Guardar libro", libro).decode()
libro_id = int(sql("SELECT id FROM libros WHERE isbn='9780134685991'"))
assert "Libro registrado" in pagina and disponibilidad() == 4
hoy = datetime.datetime.now(ZoneInfo("America/Lima")).date()
prestamo = {"prestamo:estudiante": "alumno01", "prestamo:libro": str(libro_id), "prestamo:vence": str(hoy)}
pagina = pulsar(pagina, "Prestar", prestamo).decode()
assert "posterior a hoy" in pagina
prestamo["prestamo:vence"] = str(hoy + datetime.timedelta(days=1))
for cantidad in [3, 2, 1]:
    pagina = pulsar(pagina, "Prestar", prestamo).decode()
    assert "Préstamo registrado" in pagina and disponibilidad() == cantidad
    assert f"ID {libro_id} · Java · Disponibles: {cantidad}" in pagina
pagina = pulsar(pagina, "Prestar", prestamo).decode()
assert "tres préstamos activos" in pagina
pagina = pulsar(pagina, "Registrar devolución", {"prestamoId": "1"}).decode()
assert "Devolución registrada" in pagina and disponibilidad() == 2
subprocess.run(["docker", "compose", "exec", "-T", "mysql", "sh", "-c",
                'MYSQL_PWD="$MYSQL_PASSWORD" mysql -u biblioteca biblioteca'], check=True,
               input=f"UPDATE prestamos SET avisado=NULL, inicio='{hoy-datetime.timedelta(days=8)}', vence='{hoy-datetime.timedelta(days=1)}' WHERE id=2;".encode())
xlsx = pulsar(pagina, "Descargar reportes en Excel")
with zipfile.ZipFile(io.BytesIO(xlsx)) as libro_excel:
    for i, esperado in enumerate(["3", "1"], 1):
        hoja = ET.fromstring(libro_excel.read(f"xl/worksheets/sheet{i}.xml"))
        assert hoja.find(".//{*}c[@r='C2']/{*}v").text in (esperado, esperado+".0")
pagina = ingreso(pulsar(pagina, "Cerrar sesión").decode(), "alumno01")
assert "Registrar devolución" not in pagina and "Joshua Bloch" in pagina
print("LDAP, validadores JSF, préstamo, límite, devolución, API y Excel verificados.")

# Ambos temporizadores se ejecutan cada cinco segundos únicamente en esta prueba.
for intento in range(20):
    mensajes = json.load(http.open("http://localhost:8025/api/v1/messages"))["messages"]
    entregado = any(m["Subject"] == "Préstamo vencido" and
                    any(d["Address"] == "alumno01@xyz.test" for d in m["To"]) for m in mensajes)
    if entregado:
        break
    time.sleep(2)
else:
    raise AssertionError("El buzón LDAP del estudiante no recibió el aviso de mora")

for intento in range(15):
    archivo = subprocess.run(["docker", "compose", "exec", "-T", "web", "cat",
        f"/opt/jboss/wildfly/standalone/data/reportes/{hoy}.xlsx"], capture_output=True)
    if archivo.returncode == 0:
        with zipfile.ZipFile(io.BytesIO(archivo.stdout)) as diario:
            vencidos = ET.fromstring(diario.read("xl/worksheets/sheet2.xml")).find(".//{*}c[@r='C2']/{*}v")
            if vencidos is not None and vencidos.text in ("1", "1.0"):
                break
    time.sleep(2)
else:
    raise AssertionError("Falta el reporte programado de morosidad")
print("Entrega SMTP y reporte programado verificados.")

for servicio in ("alumnos-demo", "catalogo-demo"):
    subprocess.run(["docker", "compose", "run", "--rm", "--no-deps", servicio], check=True)
assert sql("SELECT COUNT(*) FROM libros") == "13"
assert sql("SELECT COUNT(*) FROM usuarios") == "10"
assert sql("SELECT COUNT(*) FROM prestamos") == "3"
assert disponibilidad() == 2
print("Recarga verificada con datos y préstamos preexistentes.")
