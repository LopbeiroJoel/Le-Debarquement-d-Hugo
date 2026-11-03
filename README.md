# Le Débarquement d’Hugo

Prototype Java 2D de Joël et Thomas, ouvert dans VS Code via Ubuntu/WSL.

Compiler : `sh build.sh`
Lancer : `sh run.sh`
Tester les collisions : `sh test.sh`

ZQSD, WASD ou flèches : déplacement. + et - : zoom. F1 : collisions visibles. P ou Échap : pause. R : recommencer.

Le sol est séparé des objets. Les décors ont chacun leur position et leur zone de collision rectangulaire. La caméra suit un personnage de test rectangulaire. Les routes et la rivière sont dessinées en Java2D. Les PNG des objets conservent leur résolution source et sont affichés à l’échelle du monde avec un rendu pixel art.

Joël : map, décor, collisions et caméra. Thomas : personnage, tirs, XP et améliorations. `GameMap.setChestListener` transmet une ouverture de caisse à la future logique des bonus. Les bonus ne sont pas encore implémentés.

`main` contient la structure initiale. `map-entites-collisions` contient cette première scène de test et ses ressources.
