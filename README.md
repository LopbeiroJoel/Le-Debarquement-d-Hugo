# Le Débarquement d'Hugo

Jeu 2D (survivor-like) en Java avec libGDX, développé par Joël et Thomas.

## Prérequis
- JDK 25 (Temurin conseillé)
- VS Code + extension « Extension Pack for Java »

## Lancer le jeu
- macOS / Linux / WSL : `./gradlew hugo:run`
- Windows : `gradlew.bat hugo:run`

## Répartition
- **Joël** : monde, décor, collisions, caméra et interface.
- **Thomas** : personnage, tirs, statistiques et améliorations.

La map est intégrée dans `main`. Le code de la carte est regroupé dans `core/src/main/java/hugo/map/`.

## Tester la map

```bash
git fetch origin
git switch main
git pull --ff-only
./gradlew hugo:run
```

Le jeu ouvre la map avec une caméra libre.

- ZQSD, WASD ou flèches : caméra libre.
- Molette ou + / - : zoom.
- M : petite carte.
- R : retour au départ.
- F1 : collisions.

Hugo reste à coder par Thomas. Ses sprites sont disponibles dans `assets/hero-v2.png`. Les éléments de décor restent indépendants ; la map conserve ses collisions, ses deux ponts, ses six caisses et les arbres personnalisés.

## Vérifier le portage

```bash
./gradlew core:check
./gradlew hugo:run --args=--smoke-test
```

Le second contrôle ouvre une vraie fenêtre, rend 90 images sur trois zones, puis ferme l’application. Les captures sont enregistrées dans `hugo/build/smoke/`.

---

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and a main class extending `Game` that sets the first screen.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `hugo`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `hugo:jar`: builds application's runnable jar, which can be found at `hugo/build/libs`.
- `hugo:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.
