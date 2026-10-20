# Cómo trabajamos en Retroconver

Somos dos personas. La regla principal: **nadie sube código directamente a `main` y nadie aprueba su propia Pull Request.**

## Flujo de una tarea

1. **Coge una issue asignada a ti** y muévela a **En curso** en el [tablero](https://github.com/users/yeray06/projects/2). Comprueba antes que sus dependencias (*Blocked by*) estén cerradas.
2. **Crea una rama desde `main` actualizado**, con el tipo, el número de la issue y una descripción corta:

   ```bash
   git switch main
   git pull
   git switch -c feature/14-pantalla-perfil
   ```

   | Tipo | Cuándo |
   |---|---|
   | `feature/` | Funcionalidad nueva |
   | `fix/` | Corrección de un error |
   | `docs/` | README, memoria, planificación, diagramas |
   | `chore/` | Configuración, dependencias, plantillas |

3. **Haz commits pequeños**, en español y en imperativo, con el número de la issue:

   ```
   Añade formulario de edición del perfil (#14)
   Corrige el orden de las publicaciones del muro (#16)
   ```

4. **Abre una Pull Request contra `main`**:
   - Usa la plantilla y escribe `Closes #N` para que la issue se cierre al hacer merge.
   - Pide revisión al otro integrante.
   - Mueve la issue a **En revisión**.
5. **La otra persona revisa**: descarga la rama, comprueba que compila en Android Studio, prueba los criterios de terminado de la issue y comenta o aprueba.
6. **Merge**: lo hace quien revisa, con *Create a merge commit* para que se conserven los commits de cada uno. Después se borra la rama.

## Cuándo una tarea está terminada

- Cumple todos los criterios de terminado de la issue.
- `main` sigue compilando y la app arranca.
- Está revisada y aprobada por el otro integrante.
- Si cambia algo de cara al usuario, el README o la memoria están actualizados.

## Errores

Cualquier error encontrado se abre como issue con la plantilla **Error**, se asigna a quien lleve ese módulo y se añade al tablero.
