#!/bin/sh
set -eu
base='ou=personas,dc=xyz,dc=edu'
admin='cn=admin,dc=xyz,dc=edu'
umask 077
clave=$(mktemp)
trap 'rm -f "$clave"' EXIT
printf '%s' "$LDAP_ADMIN_PASSWORD" > "$clave"

i=0
until ldapsearch -x -H ldap://ldap -D "$admin" -y "$clave" -b "$base" -s base dn >/dev/null 2>&1; do
    i=$((i+1))
    [ "$i" -lt 60 ] || { echo "Directorio LDAP no disponible" >&2; exit 1; }
    sleep 2
done

while IFS='|' read -r uid nombre; do
    rc=0
    ldapadd -x -H ldap://ldap -D "$admin" -y "$clave" <<LDIF || rc=$?
dn: uid=$uid,$base
objectClass: inetOrgPerson
uid: $uid
cn: $nombre
sn: ${nombre##* }
mail: $uid@xyz.test
employeeType: estudiante-activo
userPassword: DemoXYZ2026

LDIF
    # El código LDAP 68 indica una entrada preexistente.
    case "$rc" in 0|68) ;; *) exit "$rc" ;; esac
done < /demo/alumnos.txt
