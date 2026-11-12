# Le Débarquement d’Hugo

Jeu de survie 2D développé par Joël et Thomas, en Java avec libGDX. Hugo tire automatiquement sur l’ennemi le plus proche à portée. Déplace-toi, ouvre des coffres et gagne de l’XP pour améliorer ses stats.

## Lancer le jeu

Installe un JDK 25. Depuis la racine du dépôt, lance la commande qui correspond à ton terminal.

**Ubuntu, WSL, Linux ou macOS**

```bash
./gradlew hugo:run
```

**Windows PowerShell**

```powershell
.\gradlew.bat hugo:run
```

La cinématique dure 30 secondes. Appuie sur **Échap** pour la passer, puis clique sur **Start** ou appuie sur **Entrée** pour jouer.

## Commandes

| Commande | Action |
| --- | --- |
| ZQSD, WASD ou flèches | Déplacer Hugo |
| Molette ou touches + et − | Régler le zoom |
| M | Afficher ou masquer la mini-carte |
| R | Réinitialiser le zoom et les coffres |
| F1 | Afficher ou masquer les collisions |
| F8 | Supprimer les mobs et leurs projectiles |
| F9 | Activer ou désactiver le mode Goku |

## Vagues et progression

La première vague arrive au lancement de la partie. Les suivantes se déclenchent toutes les **25 secondes de jeu**, même si des ennemis sont encore vivants. Un cycle compte cinq vagues de **5, 13, 20, 25 et 30 mobs**. Le boss arrive avec la cinquième vague. Le cycle suivant repart à la vague 1 après **2 min 05 de jeu**.

L’XP gagnée dépend du type de mob et du cycle. Le premier niveau à gagner demande **50 XP**. À chaque montée de niveau, le jeu se met en pause et propose **trois améliorations**. Les barres affichent la progression de Hugo et les PV au-dessus des personnages.

## Coffres

Approche-toi d’un coffre pour l’ouvrir. Le coffre se referme après **deux minutes de jeu**. Le chrono des vagues est en pause pendant les menus.

Les cartes suivantes sont disponibles sur la branche `animation`.

| Carte | Effet |
| --- | --- |
| Soin complet | Rend tous les PV à Hugo. |
| Maté | Multiplie la vitesse par 2,25 et le délai entre les tirs par 0,175 pendant 30 secondes. |
| Tabac | Coûte la moitié des PV actuels, triple les dégâts et multiplie le délai entre les tirs par 0,15 pendant 30 secondes. |

Les boosts du Maté et du Tabac démarrent après leur animation.

## Développement

- **Joël** travaille sur le monde, le décor, les collisions, la caméra et l’interface.
- **Thomas** travaille sur le personnage, les tirs, les statistiques et les améliorations.
- `core/` contient le code du jeu et `hugo/` le lanceur de bureau LWJGL3.
- `assets/` contient les sprites, la carte et la cinématique.

```bash
./gradlew core:check
./gradlew hugo:run --args=--smoke-test
```

Le test de lancement ouvre directement la carte, rend 90 images sur trois zones et se termine. Les captures sont dans `hugo/build/smoke/`. La commande `./gradlew hugo:jar` produit le JAR dans `hugo/build/libs/`.
