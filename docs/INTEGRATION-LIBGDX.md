# Intégration sur le main de Thomas

La branche `map-libgdx` part du commit `fee883c` de `main`. Le code de map a été repris dans la structure gdx-liftoff, sans fusion des anciennes racines de projet.

## Structure conservée

- `core` : logique et écrans partagés.
- `lwjgl3` : lanceur desktop et configuration native.
- `assets` : ressources du jeu.
- Package racine : `hugo.epitech`.
- JDK 25, libGDX 1.14.2, LWJGL 3.4.3 et wrapper Gradle du main de Thomas.
- Commande de lancement : `./gradlew lwjgl3:run`.

## Écran de map

`HugoGame` ouvre `hugo.epitech.screen.MapScreen`, un `ScreenAdapter` libGDX. Le fichier `FirstScreen` de Thomas reste disponible. Les ressources sont chargées dans `show`, libérées dans `dispose`, et l’InputProcessor est détaché dans `hide`.

`MapScreen` gère la caméra libre et le HUD. Aucun personnage, tir, effet de bonus ou déplacement de joueur n’y est codé.

## Monde et personnage

Les classes de monde se trouvent dans `core/src/main/java/hugo/epitech/world/` :

- `GameMap` : objets, zones, collisions et événements d’ouverture des caisses.
- `MapRenderer` : affichage du sol, des objets et des ombres.
- `MapEntity` : position et collision d’un élément indépendant.
- `Sprites` : textures pixel art et calcul des ombres au sol.
- `Camera` : limites du cadrage.

Thomas pourra créer son joueur et utiliser `GameMap.canMove` ou `GameMap.move`. Pour l’affichage, `MapRenderer.draw` accepte la position verticale des pieds et un callback de dessin ; le SpriteBatch est déjà ouvert lorsque ce callback est appelé. Les paramètres actuels de la boîte de collision de test sont 22 × 17 unités, à adapter au joueur.

Les caisses s’ouvrent avec `GameMap.updateTriggers` et notifient `setChestListener`. La caméra libre ne déclenche pas ces événements.

Les sprites d’Hugo sont conservés dans `assets/hero-v2.png` et `assets/hero.png`. La nouvelle planche contient quatre cellules horizontales de 543 × 724 pixels, avec fond transparent.

## Ressources

- `assets/maps/world.json` : 854 éléments, leurs positions et la géométrie des zones.
- `assets/decor` et `assets/coffres` : PNG sources, dont les versions HD v3.
- `assets/render/ground` : sol seul découpé en tuiles.
- `assets/render/sprites` : textures préparées, avec repli sur les sources pour les nouvelles versions.

Les palissades verticales ont été retirées avec leurs collisions. Les routes restent dégagées. Les ombres sont rattachées au contour inférieur des objets pour éviter l’effet de flottement.

## Vérification

`./gradlew core:check` contrôle les limites, les 825 collisions d’objets, les murs des tranchées, les ponts, l’accès aux six caisses et à la clairière, les ouvertures, les zones et les limites de caméra.

`./gradlew lwjgl3:run --args=--smoke-test` charge les textures en OpenGL, rend le départ, la base et les tranchées, écrit trois PNG dans `lwjgl3/build/smoke` et ferme la fenêtre. Vérifié sur Ubuntu WSL ARM64 avec JDK 25.

Le lanceur garde le StartupHelper généré par gdx-liftoff et sélectionne X11 uniquement sous WSL. Il conserve la configuration audio de la base de Thomas.
