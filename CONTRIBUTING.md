# Cómo trabajamos en Retroconver

Somos dos personas. La regla principal: **nadie sube código directamente a `main` y nadie aprueba su propia Pull Request.**

## Cómo están organizadas las tareas

- **Código de orden.** Cada issue empieza por un código que indica cuándo toca hacerla. `E2-01` es la primera tarea principal de la entrega 2, y `E2-01.1` y `E2-01.2` son sus subtareas. `EF` es la entrega final y `EX`, los extras. El [tablero](https://github.com/users/yeray06/projects/2) está ordenado por este código, así que se lee de arriba abajo en el orden en que hay que avanzar.
- **Tareas principales y subtareas.** Una tarea principal agrupa el trabajo de una funcionalidad. Sus subtareas (*sub-issues* de GitHub) son lo que se hace de verdad: **una subtarea, una rama, una Pull Request**. Si una tarea principal no tiene subtareas, se trabaja directamente sobre ella.
- **Número de GitHub.** El número (#7, #34…) es el que se usa en ramas, commits y Pull Requests. El código solo sirve para ordenar.
- **Dependencias.** Cada issue indica en *Blocked by* qué tiene que estar terminado antes. Ninguna tarea depende de otra con un código posterior.

## Flujo de una tarea

1. **Coge la siguiente subtarea asignada a ti**: la de código más bajo que no esté bloqueada. Muévela a **En curso** en el tablero.
2. **Crea una rama desde `main` actualizado**, con el tipo, el número de la issue y una descripción corta:

   ```bash
   git switch main
   git pull
   git switch -c feature/53-vista-perfil
   ```

   | Tipo | Cuándo |
   |---|---|
   | `feature/` | Funcionalidad nueva |
   | `fix/` | Corrección de un error |
   | `docs/` | README, memoria, planificación, diagramas |
   | `chore/` | Configuración, dependencias, plantillas |

3. **Haz commits pequeños**, en español y en imperativo, con el número de la issue:

   ```
   Añade la vista del perfil con su ViewModel (#53)
   Corrige el orden de las publicaciones del muro (#58)
   ```

4. **Abre una Pull Request contra `main`**:
   - Usa la plantilla y escribe `Closes #N` con el número de la subtarea, para que se cierre al hacer merge.
   - Pide revisión al otro integrante.
   - Mueve la subtarea a **En revisión**.
5. **La otra persona revisa**: descarga la rama, comprueba que compila en Android Studio, prueba los criterios de terminado de la subtarea y comenta o aprueba.
6. **Merge**: lo hace quien revisa, con *Create a merge commit* para que se conserven los commits de cada uno. Después se borra la rama.
7. **Cierra la tarea principal** cuando todas sus subtareas estén cerradas. La barra de progreso del tablero lo indica. Si la PR es la de la última subtarea, puede llevar también `Closes` con el número de la tarea principal.

## Cuándo una tarea está terminada

- Cumple todos los criterios de terminado de la issue.
- `main` sigue compilando y la app arranca.
- Está revisada y aprobada por el otro integrante.
- Si cambia algo de cara al usuario, el README o la memoria están actualizados.

Una tarea principal está terminada cuando lo están todas sus subtareas y se cumplen sus propios criterios.

## Tareas nuevas

Si aparece trabajo que no estaba previsto:

1. Créalo con la plantilla **Tarea** y asígnalo.
2. Ponle código:
   - Si es parte de una tarea principal, toma el siguiente número de subtarea (`E3-06.4`) y añádelo como *sub-issue* de esa tarea.
   - Si es una tarea nueva, toma el siguiente código libre de su entrega.
3. Asígnale el milestone de su entrega y marca sus dependencias en *Blocked by*.

## Nombres de ficheros prohibidos en Windows

No crees ficheros ni carpetas llamados `aux`, `con`, `nul`, `prn`, `com1`… `com9` o `lpt1`… `lpt9`, con o sin extensión (`aux.txt` tampoco vale). Windows no los admite y el repositorio deja de poder clonarse en Windows.

Para crear una carpeta nueva desde la web de GitHub no hace falta un fichero auxiliar: en *Add file → Create new file* escribe la ruta completa en el nombre, por ejemplo `docs/datos/esquema.md`, y la carpeta se crea sola.

## Errores

Cualquier error encontrado se abre como issue con la plantilla **Error**, se asigna a quien lleve ese módulo y se añade al tablero.
