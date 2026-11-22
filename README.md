# Le Débarquement d’Hugo

Jeu de survie 2D de Joël et Thomas, développé en Java 25 avec libGDX 1.14.2. Hugo tire automatiquement sur le mob le plus proche à portée. Déplace-toi, récupère de l’XP et ouvre des coffres pour survivre aux vagues.

## Lancer le jeu

Il faut un **JDK 25**. Ouvre le dossier complet du dépôt dans VS Code, puis son terminal.

Sous Linux, WSL ou macOS :

```bash
./gradlew hugo:run
```

Sous Windows PowerShell :

```powershell
.\gradlew.bat hugo:run
```

La cinématique précède le menu. **Échap** permet de la passer ; clique sur **Start** ou appuie sur **Entrée** pour jouer. Si la vidéo ne peut pas être lue, le menu reste accessible.

## Commandes

| Touche | Action |
| --- | --- |
| ZQSD, WASD ou flèches | Déplacer Hugo |
| Molette, + ou − | Changer le zoom |
| M | Afficher ou masquer la mini-carte |
| R | Réinitialiser le zoom et les coffres |
| F1 | Afficher les collisions et les informations de débogage |
| F8 | Retirer les mobs et leurs projectiles (admin) |
| F9 | Activer ou désactiver le mode Goku |

Ferme la fenêtre pour quitter. Dans un terminal qui exécute Gradle, `Ctrl+C` arrête aussi le jeu.

## Vagues et progression

La vague 1 arrive dès le début. Une nouvelle vague apparaît toutes les **25 secondes de jeu**, sans supprimer les survivants. Les menus de coffre, d’amélioration et de fin de partie suspendent la simulation.

| Vague | Mob 1 | Chien (2) | Machette (3) | Tireur (4) | Boss (5) | Total |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | 5 | 0 | 0 | 0 | 0 | 5 |
| 2 | 5 | 8 | 0 | 0 | 0 | 13 |
| 3 | 5 | 5 | 10 | 0 | 0 | 20 |
| 4 | 5 | 5 | 5 | 10 | 0 | 25 |
| 5 | 4 | 5 | 8 | 12 | 1 | 30 |

Après cinq vagues, le cycle recommence. Le multiplicateur de dégâts est `1.2^(cycle - 1)` ; l’XP utilise la même croissance, avec un arrondi. Les récompenses de base sont **10, 20, 40, 80 et 250 XP** selon le mob. Le premier niveau demande **50 XP**, puis le coût est multiplié par **1,3**. Chaque niveau gagné propose trois améliorations pondérées sans doublon.

Les cinq types de mobs ont leurs animations. Les valeurs actuelles restent celles du prototype : les mobs 2 à 5 et les projectiles ennemis ont encore des dégâts à zéro. L’équilibrage des attaques n’a pas été modifié lors du rangement et de la traduction.

## Armes

Hugo commence avec une carabine. Le système d’améliorations peut proposer un pistolet-mitrailleur, une mitrailleuse ou un fusil à pompe. Chaque arme a ses dégâts, son délai et sa portée. Le fusil à pompe produit plusieurs projectiles. Le sprite tenu par Hugo change avec l’arme équipée.

## Coffres et bonus

Approche-toi d’un coffre : il s’ouvre et propose **trois cartes différentes tirées au hasard**. Choisis une carte ou ferme le menu. Le coffre reste ouvert deux minutes de jeu, puis se referme. Il faut sortir de sa zone et revenir pour le réutiliser.

| Bonus | Effet actuel |
| --- | --- |
| Full heal | Rend toute la vie et joue l’animation du cœur. |
| Mate | Vitesse ×2,25 et délai entre les tirs ×0,175 pendant 30 secondes. |
| Tobacco | Retire la moitié des PV actuels, dégâts ×3 et délai entre les tirs ×0,15 pendant 30 secondes. |
| Helmet | Empêche Hugo de subir des dégâts pendant 30 secondes. |
| Parachute | Élimine les mobs présents et joue l’arrivée des parachutistes. |
| Whisky | Anime Hugo en train de boire, puis produit un effet visuel d’ivresse pendant 30 secondes. |

Les chronomètres du Maté, du Tabac, du Casque et du Whisky démarrent après leur animation. Les multiplicateurs temporaires se retirent à la fin sans supprimer les améliorations permanentes de `Stat`. Hugo régénère aussi 1 PV par seconde tant qu’il est vivant.

## Où se trouve le code ?

| Dossier | Contenu |
| --- | --- |
| `core/src/main/java/hugo/entity` | Personnage, créatures, vie, statistiques, projectiles et sprites d’armes |
| `core/src/main/java/hugo/map` | Monde, décor, collisions, rendu, caméra et écran de jeu |
| `core/src/main/java/hugo/mob` | Mobs, spawn, espacement et récompenses XP |
| `core/src/main/java/hugo/wave` | Vagues et cycles |
| `core/src/main/java/hugo/weapon` | Armes et tirs |
| `core/src/main/java/hugo/bonus` | Bonus et animations |
| `core/src/main/java/hugo/chest` | Tirage et menu des bonus |
| `core/src/main/java/hugo/upgrade` | Améliorations à la montée de niveau |
| `core/src/main/java/hugo/startgame` | Cinématique et menu Start |
| `core/src/main/java/hugo/ui`, `hugo/xp`, `hugo/gameover`, `hugo/admin` | Barres, fin de partie et outils admin |
| `hugo/src/main/java/hugo/desktop` | Lanceur LWJGL3 : `DesktopLauncher` |
| `core/src/test/java` | Vérifications automatiques |
| `assets` | Images, carte JSON, sons et vidéo |

Les identifiants, commentaires et textes définis en Java sont en anglais. Le titre du jeu, les chemins d’assets existants et les textes déjà inclus dans les images ou vidéos sont conservés.

Le [diagramme UML](docs/diagramme-uml.md) présente les liens entre les classes des deux développeurs. La [version PlantUML](docs/UML.puml) contient les mêmes cinq vues.

## Vérifications

```bash
./gradlew core:check
./gradlew hugo:run --args=--smoke-test
./gradlew hugo:jar
```

Sous PowerShell, remplace `./gradlew` par `.\gradlew.bat`.

`core:check` vérifie la carte, les collisions, les coffres, les armes, le tirage des améliorations et les cycles de vagues. Le test de lancement ouvre la carte, rend 90 images et s’arrête. Les captures sont enregistrées dans `hugo/build/smoke/`. Il ne remplace pas un test complet de partie.

Le JAR est produit dans `hugo/build/libs/` et se lance avec `java -jar chemin/vers/le-jeu.jar`, avec Java 25. Les tâches de création d’installateurs natifs utilisent aussi un JDK 25.
