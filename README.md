# Le Débarquement d’Hugo

Projet Java migré vers libGDX, avec le backend desktop LWJGL3. Le jeu n’utilise plus Swing, AWT ni Java2D.

Les branches `MAP` et `PERSONNAGE-PRINCIPAL` utilisent cette base libGDX. Hugo est retiré du programme pour que Thomas puisse coder le personnage ; ses PNG restent disponibles dans les ressources.

## Lancer avec Ubuntu et VS Code

```bash
cd ~/projects/Le-Debarquement-d-Hugo
sh run.sh
```

Un JDK 17 ou plus récent est nécessaire. Le wrapper Gradle télécharge automatiquement les dépendances au premier lancement. Ubuntu/WSLg ARM64 avec Java 25 a été vérifié sur cette machine. Le lanceur sélectionne X11 sous WSL pour conserver une fenêtre avec ses bordures.

```bash
sh build.sh
sh test.sh
```

Dans VS Code : tâches **Compiler**, **Lancer**, **Tester**, ou configuration **Lancer libGDX** après import du projet Gradle.

## Commandes

- ZQSD, WASD ou flèches : caméra libre.
- Molette ou + / - : zoom.
- M : petite carte.
- R : caméra au départ et reset des caisses.
- F1 : collisions et coordonnées.

La caméra ne représente pas un joueur et ne déclenche pas les caisses.

## Organisation

- `Main.java` : lancement desktop LWJGL3.
- `game/MapGame.java` : boucle libGDX, entrées clavier, caméra et interface.
- `world/GameMap.java` : données du monde, collisions, déplacements et événements des caisses.
- `world/MapEntity.java` : un objet indépendant du décor.
- `world/MapRenderer.java` : rendu OpenGL avec SpriteBatch et affichage des collisions.
- `world/Sprites.java` : textures avec filtrage Nearest pour garder le pixel art net.
- `src/main/resources/maps/world.json` : positions des 860 objets, zones et géométrie des routes et de la rivière.
- `src/main/resources/render/ground/` : sol seul, en tuiles de 1 024 pixels maximum.
- `src/main/resources/render/sprites/` : sprites préparés pour le rendu, sans agrandissement artificiel des sources.
- `src/main/resources/assets/` : PNG sources conservés, dont les assets d’Hugo.

La map mesure 4 608 × 3 200 unités. Elle comporte la forêt, le village détruit, la base militaire, les tranchées, la rivière avec deux ponts et la batterie d’artillerie. Les arbres de Joël et Thomas sont dans la clairière au nord-ouest, en 650, 850 et 950, 850. Aucune silhouette d’arbre ou de buisson ne recouvre une route.

Le sol préparé ne contient pas les objets : chaque décor reste modifiable et possède sa propre collision. Les modèles et les tests utilisent la géométrie libGDX et fonctionnent sans fenêtre OpenGL.

## Intégration du personnage par Thomas

Thomas peut ajouter son personnage dans la boucle de `MapGame`, appeler `GameMap.canMove` ou `GameMap.move` pour les collisions, puis fournir son dessin au callback de `MapRenderer.draw`, avec la position des pieds pour le tri de profondeur. Le callback s’exécute pendant que le SpriteBatch est ouvert.

`GameMap.updateTriggers` et `setChestListener` transmettent une ouverture de caisse. La logique des bonus, de l’XP, des tirs et des ennemis reste à développer.

## Vérifications

`sh test.sh` exécute `worldTests` via Gradle : limites, collisions, caisses accessibles, ponts, absence de végétation sur les routes, caméra, profondeur et zones.

```bash
sh run.sh --args=--smoke-test
```

Ce contrôle ouvre une vraie fenêtre libGDX, charge toutes les textures, rend 30 images, écrit `build/libgdx-smoke.png` et ferme la fenêtre. Il a été exécuté avec succès sur Ubuntu ARM64.

Versions : libGDX 1.14.2, LWJGL 3.4.3, Gradle 9.2.1. Le wrapper vérifie le SHA-256 de la distribution Gradle. La version de LWJGL est adaptée à Java 25.

Références : [documentation libGDX](https://libgdx.com/wiki/start/project-generation), [compatibilité du lanceur officiel](https://github.com/libgdx/gdx-liftoff).
Les prompts des visuels restent dans `docs/asset-prompts.json`.
