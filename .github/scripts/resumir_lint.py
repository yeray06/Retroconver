"""Pasa el informe XML de Android lint a una anotación de GitHub.

Así los avisos se ven en la pestaña «Checks» de la PR sin descargar el informe.
Uso: python3 resumir_lint.py app/build/reports/lint-results-debug.xml
"""
import os
import sys
import xml.etree.ElementTree as ET

MAXIMO = 150


def escapar(texto: str) -> str:
    return texto.replace("%", "%25").replace("\r", "").replace("\n", "%0A")


def main(ruta: str) -> None:
    if not os.path.isfile(ruta):
        print(f"No hay informe de lint en {ruta}")
        return
    raiz = ET.parse(ruta).getroot()
    lineas = []
    errores = 0
    for issue in raiz.iter("issue"):
        gravedad = issue.get("severity", "")
        if gravedad in ("Error", "Fatal"):
            errores += 1
        sitio = issue.find("location")
        fichero = sitio.get("file", "") if sitio is not None else ""
        fichero = fichero.split("/app/", 1)[-1] if "/app/" in fichero else fichero
        linea = sitio.get("line", "") if sitio is not None else ""
        lineas.append(f"{gravedad} · {issue.get('id')} · {fichero}:{linea} · {issue.get('message')}")
    if not lineas:
        print("::notice title=Android lint::Sin avisos")
        return
    cuerpo = "\n".join(sorted(lineas)[:MAXIMO])
    if len(lineas) > MAXIMO:
        cuerpo += f"\n… y {len(lineas) - MAXIMO} más (informe-lint)"
    tipo = "error" if errores else "warning"
    print(f"::{tipo} title=Android lint: {len(lineas)} avisos ({errores} errores)::{escapar(cuerpo)}")


if __name__ == "__main__":
    main(sys.argv[1])
