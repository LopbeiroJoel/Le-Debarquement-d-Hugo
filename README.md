# Le Débarquement d’Hugo

Jeu Java 2D de Joël et Thomas.

- `MAP` : la nouvelle map et ses décors, avec une caméra libre.
- `PERSONNAGE-PRINCIPAL` : conserve les assets du personnage pour que Thomas puisse le coder.
- `main` : structure initiale.

## Lancer dans Ubuntu / VS Code

```bash
cd ~/projects/Le-Debarquement-d-Hugo
sh run.sh
```

Le JDK et la version graphique du JRE doivent être installés. La version headless seule ne permet pas d’ouvrir le jeu. Cette machine utilise `openjdk-25-jre` avec WSLg.

## Commandes

- ZQSD, WASD ou flèches : déplacement de la caméra libre.
- Molette ou + / - : zoom.
- M : afficher ou masquer la carte.
- R : retour au départ et réinitialisation des caisses.
- F1 : collisions et coordonnées.

## Le monde

La map mesure 4 608 × 3 200 unités, environ neuf fois la surface du premier prototype : forêt au nord, village en ruine, base militaire clôturée, tranchées, rivière avec deux ponts et batterie d’artillerie au sud. Le bocage relie les zones.

Une clairière au nord-ouest accueille les arbres personnalisés de Joël (650, 850) et Thomas (950, 850). Le chemin forestier y mène. Aucune silhouette d’arbre ou de buisson ne recouvre les routes.

Les éléments du décor sont indépendants. Les sols, routes et berges sont composés séparément. Les obstacles ont des collisions rectangulaires ajustables ; les rives suivent la forme de la rivière et les deux ponts restent traversables.

Les PNG gardent leur résolution source. Le cache conserve trois pixels par unité de jeu, et le rendu à l’écran utilise le plus proche voisin pour un pixel art net. Les maisons et les chars ont été redimensionnés par rapport à Hugo.

Hugo a été retiré du programme : chargement des sprites, rendu, animation et déplacements. Thomas peut coder le personnage. Les assets restent disponibles dans les ressources. La caméra libre permet de parcourir la map et ne déclenche aucune caisse. Les tirs, les ennemis et les effets des bonus restent à intégrer. Les caisses déclenchent une seule fois `GameMap.setChestListener` sans donner encore de bonus.

## Vérification

```bash
sh test.sh
```

Les tests couvrent les limites, collisions, accès aux six caisses depuis le départ, ponts, reset, caméra, présence des assets, accès à la clairière et absence de végétation sur les routes.

Joël : map, décors, collisions, caméra et interface. Thomas : personnage, tirs, XP et améliorations.

Les prompts des images créées avec l’outil image_gen intégré sont dans `docs/asset-prompts.json`.
