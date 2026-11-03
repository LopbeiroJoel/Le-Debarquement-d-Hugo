# Le DÃƒÂ©barquement dÃ¢â‚¬â„¢Hugo

Prototype Java 2D de JoÃƒÂ«l et Thomas. Le dÃƒÂ©veloppement de la map se trouve sur `map-entites-collisions`. `main` conserve la structure initiale.

## Lancer dans Ubuntu / VS Code

```bash
cd ~/projects/Le-Debarquement-d-Hugo
sh run.sh
```

Un JDK et le JRE avec affichage graphique sont nÃƒÂ©cessaires. La version headless seule ne permet pas dÃ¢â‚¬â„¢ouvrir le jeu. Sur cette machine, la bibliothÃƒÂ¨que graphique est fournie par `openjdk-25-jre` et WSLg.

## Commandes

- ZQSD, WASD ou flÃƒÂ¨ches : dÃƒÂ©placement.
- Molette ou + / - : zoom.
- M : afficher ou masquer la carte.
- P ou Ãƒâ€°chap : pause.
- R : retour au dÃƒÂ©part et rÃƒÂ©initialisation des caisses.
- F1 : afficher les collisions et les coordonnÃƒÂ©es.

## La map

Le monde mesure 4 608 Ãƒâ€” 3 200 unitÃƒÂ©s, environ neuf fois la surface du premier prototype. Il comprend une forÃƒÂªt au nord, un village dÃƒÂ©truit, une base militaire clÃƒÂ´turÃƒÂ©e, des tranchÃƒÂ©es, une riviÃƒÂ¨re avec deux ponts et une batterie dÃ¢â‚¬â„¢artillerie au sud. Le bocage relie ces zones. Une clairière au nord-ouest accueille les deux arbres personnalisés de Joël et Thomas. La silhouette entière des arbres et des buissons est exclue des routes.

Les dÃƒÂ©cors sont des entitÃƒÂ©s indÃƒÂ©pendantes. Le sol, les routes, les berges et les textures des diffÃƒÂ©rentes zones sont composÃƒÂ©s sÃƒÂ©parÃƒÂ©ment. Les obstacles utilisent des collisions rectangulaires ajustables ; les rives suivent la forme de lÃ¢â‚¬â„¢eau et les passages sur les ponts restent ouverts.

Les PNG conservent leur rÃƒÂ©solution source. Le cache des sprites conserve trois pixels par unitÃƒÂ© du monde, au lieu de rÃƒÂ©duire les images ÃƒÂ  une minuscule texture avant de zoomer. Le rendu ÃƒÂ  lÃ¢â‚¬â„¢ÃƒÂ©cran utilise le plus proche voisin pour garder un aspect pixel art net. La camÃƒÂ©ra est limitÃƒÂ©e pour prÃƒÂ©server la lisibilitÃƒÂ©.

Hugo dispose dÃ¢â‚¬â„¢un sprite de prÃƒÂ©sentation fixe. Les tirs, les ennemis, les effets des bonus et lÃ¢â‚¬â„¢animation du personnage restent ÃƒÂ  intÃƒÂ©grer par Thomas. Une caisse dÃƒÂ©clenche une seule fois le callback `GameMap.setChestListener` ; ce callback ne donne pas encore de bonus.

## VÃƒÂ©rification

```bash
sh test.sh
```

Les tests vÃƒÂ©rifient les limites, les collisions, les deux ponts, lÃ¢â‚¬â„¢accÃƒÂ¨s aux six caisses depuis le dÃƒÂ©part, les ÃƒÂ©vÃƒÂ©nements dÃ¢â‚¬â„¢ouverture, le reset, la camÃƒÂ©ra et la prÃƒÂ©sence des dÃƒÂ©cors.

## RÃƒÂ©partition

JoÃƒÂ«l : map, dÃƒÂ©cor, collisions, camÃƒÂ©ra et interface. Thomas : personnage, tirs, XP et amÃƒÂ©liorations.

Les prompts des nouveaux assets crÃƒÂ©ÃƒÂ©s avec lÃ¢â‚¬â„¢outil image_gen intÃƒÂ©grÃƒÂ© sont conservÃƒÂ©s dans `docs/asset-prompts.json`.
