# Intégration sur le main de Thomas

La branche `map-libgdx` part du commit `fee883c` de `main`. Le code de map a été repris dans la structure gdx-liftoff, sans fusion des anciennes racines de projet.

## Structure conservée

- `core` : logique et écrans partagés.
- `lwjgl3` : lanceur desktop et configuration native.
- `assets` : ressources du jeu.
- Package racine : `hugo`.
- JDK 25, libGDX 1.14.2, LWJGL 3.4.3 et wrapper Gradle du main de Thomas.
- Commande de lancement : `./gradlew lwjgl3:run`.

## Écran de map

`JeuHugo` ouvre `hugo.map.EcranCarte`, un `ScreenAdapter` libGDX. Le fichier `PremierEcran` de Thomas reste disponible. Les ressources sont chargées dans `show`, libérées dans `dispose`, et l’InputProcessor est détaché dans `hide`.

`EcranCarte` gère la caméra libre et le HUD. Aucun personnage, tir, effet de bonus ou déplacement de joueur n’y est codé.

## Monde et personnage

Les classes de monde se trouvent dans `core/src/main/java/hugo/map/` :

- `CarteJeu` : objets, zones, collisions et événements d’ouverture des caisses.
- `RenduCarte` : affichage du sol, des objets et des ombres.
- `ObjetDecor` : position et collision d’un élément indépendant.
- `ImagesDecor` : textures pixel art et calcul des ombres au sol.
- `CameraCarte` : limites du cadrage.

Thomas pourra créer son joueur et utiliser `CarteJeu.canMove` ou `CarteJeu.move`. Pour l’affichage, `RenduCarte.draw` accepte la position verticale des pieds et un callback de dessin ; le SpriteBatch est déjà ouvert lorsque ce callback est appelé. Les paramètres actuels de la boîte de collision de test sont 22 × 17 unités, à adapter au joueur.

Les caisses s’ouvrent avec `CarteJeu.updateTriggers` et notifient `setChestListener`. La caméra libre ne déclenche pas ces événements.

La planche actuelle d’Hugo est conservée dans `assets/hero-v2.png`. La nouvelle planche contient quatre cellules horizontales de 543 × 724 pixels, avec fond transparent.

## Ressources

- `assets/maps/carte.json` : 854 éléments, leurs positions et la géométrie des zones.
- `assets/decor` et `assets/coffres` : PNG sources, dont les versions HD v3.
- `assets/render/ground` : sol seul découpé en tuiles.
- `assets/render/sprites` : textures préparées, avec repli sur les sources pour les nouvelles versions.

Les palissades verticales ont été retirées avec leurs collisions. Les routes restent dégagées. Les ombres sont rattachées au contour inférieur des objets pour éviter l’effet de flottement.

## Vérification

`./gradlew core:check` contrôle les limites, les 825 collisions d’objets, les murs des tranchées, les ponts, l’accès aux six caisses et à la clairière, les ouvertures, les zones et les limites de caméra.

`./gradlew lwjgl3:run --args=--smoke-test` charge les textures en OpenGL, rend le départ, la base et les tranchées, écrit trois PNG dans `lwjgl3/build/smoke` et ferme la fenêtre. Vérifié sur Ubuntu WSL ARM64 avec JDK 25.

Le lanceur garde le AideDemarrage généré par gdx-liftoff et sélectionne X11 uniquement sous WSL. Il conserve la configuration audio de la base de Thomas.

## Fichiers de la carte

- `EcranCarte.java` : commandes, zoom et affichage de l’écran.
- `CarteJeu.java` : chargement, déplacements autorisés et coffres.
- `ObjetDecor.java` : un objet avec sa position et sa collision.
- `TypeDecor.java` : catalogue des images, tailles et formes des objets.
- `ZoneCarte.java` : contours des routes, zones et rivière.
- `CameraCarte.java` : cadrage et limites de caméra.
- `RenduCarte.java` : dessin du sol, des objets et de la petite carte.
- `ImagesDecor.java` : chargement des images et ombres.

Le sol se trouve dans `assets/render/ground/`. Les objets sont placés dans `assets/maps/carte.json`. Leurs collisions sont calculées en Java dans `ObjetDecor` et `CarteJeu`, aux mêmes coordonnées.
