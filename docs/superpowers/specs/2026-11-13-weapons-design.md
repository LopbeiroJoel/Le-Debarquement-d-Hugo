# Les armes de Hugo — design

*13 novembre 2026 · Thomas (avec Claude) · statut : à valider*

## Intention

Donner au joueur **plusieurs façons de jouer** Hugo. Chaque arme a un caractère (rapide et faible, lente et large, rare et puissante…) et le joueur **change d'arme selon son style**, pas parce qu'une arme est simplement meilleure.

## Ce qui est décidé (par Thomas)

- Hugo porte **une seule arme à la fois** ; en prendre une nouvelle **remplace** l'ancienne.
- Il démarre avec la **carabine** (le tir actuel).
- Les **améliorations de Hugo** (cartes « +20 % de dégâts », « −10 % de délai de tir », portée, critiques) **s'appliquent à toutes les armes** : changer d'arme ne fait pas perdre la progression.
- Construction choisie : **une classe abstraite `Weapon` et une sous-classe par façon de tirer** (option A).
- Les 5 armes et leur caractère :

| Arme | Caractère voulu |
|---|---|
| Carabine | arme de base |
| Pistolet-mitrailleur (PM) | peu de dégâts, cadence très rapide |
| Fusil à pompe | gros dégâts, en zone (éventail), **lent** |
| Mitrailleuse | gros dégâts, cadence moyenne, **rare** |
| Lance-flammes | dégâts de zone |

- **Obtention** (XP, coffres ou les deux) : **pas encore tranché** — le système doit permettre les deux sans rien refaire.

## Objectifs

1. Sortir le tir de `Player` vers une classe `Weapon`, sans changer le comportement actuel (la carabine tire exactement comme aujourd'hui).
2. Les 5 armes jouables, chacune avec sa forme de tir.
3. Une arme s'obtient par une **carte dans le menu de niveau** ; une méthode `player.equipWeapon(...)` permet aussi aux coffres de Joël d'en donner une.
4. La mitrailleuse sort **plus rarement** que les autres cartes.

## Hors sujet (pour plus tard)

- Plusieurs armes en même temps.
- Des niveaux propres à chaque arme (« carabine niveau 3 »), munitions, rechargement.
- Afficher l'arme dans les mains de Hugo (son sprite garde son fusil).
- Brancher les armes dans les coffres : c'est la partie de Joël, `equipWeapon` est prêt pour lui.

## À éviter absolument

- **Perdre la progression** en changeant d'arme.
- **Une arme qui écrase toutes les autres** : chacune paie ses points forts (portée, cadence, dégâts).
- **Casser le code de Joël** : ses mobs tireurs utilisent aussi `Projectile` ; tout ajout à `Projectile` doit garder ses constructeurs actuels.

## Approches envisagées

| | Approche | Pour | Contre |
|---|---|---|---|
| **A (choisie)** | `Weapon` abstraite + sous-classes par forme de tir | lisible, chaque forme de tir isolée ; même schéma que `Upgrade` ; montre le polymorphisme | plus de fichiers |
| B | une seule classe `Weapon` réglée par des chiffres | peu de fichiers | le lance-flammes rentre mal ; longue liste de paramètres |
| C | tout dans `Player` avec un `switch` | rapide | `Player` déjà très chargé ; écartée |

## Le design

### Classes (package `hugo.weapon`)

| Classe | Rôle |
|---|---|
| `Weapon` (abstraite) | nom, nom d'image, dégâts de base, délai de base, portée de base, vitesse et taille des balles ; méthode abstraite `fire(...)` qui renvoie la **liste** des balles tirées |
| `SingleShotWeapon` | une balle droite vers la cible ; sert pour **carabine, PM, mitrailleuse** (seuls les chiffres changent) |
| `ShotgunWeapon` | N plombs répartis en éventail autour de la direction de la cible |
| `FlamethrowerWeapon` | flammes courtes, larges, **perçantes** (traversent les mobs) |
| `Weapons` | crée les 5 armes avec leurs chiffres (un seul endroit pour l'équilibrage) |

`fire` renvoie une `List<Projectile>` car le fusil à pompe tire plusieurs balles à la fois ; `EcranCarte` ajoute toute la liste à `projectiles`.

### Comment se combinent l'arme et les améliorations de Hugo — *choix de Claude*

Aujourd'hui `attackDamage` (8), `fireDelay` (1 s) et `attackRange` (500) de Hugo sont des valeurs absolues. Ils deviennent des **multiplicateurs** qui valent **1** au départ :

```
dégâts d'une balle = dégâts de base de l'arme × hugo.getAttackDamage().getValue()
délai entre deux tirs = délai de base de l'arme × hugo.getFireDelay().getValue()
portée = portée de base de l'arme × hugo.getAttackRange().getValue()
```

- Les cartes existantes (« +20 % de dégâts » = ×1,2, « −10 % de délai » = ×0,9, « +10 % de portée » = ×1,1) marchent **sans changement**, pour toutes les armes.
- La **carabine** reprend les valeurs actuelles (8 de dégâts, 1 s, 500 de portée) : le jeu se comporte exactement comme aujourd'hui.
- Les **critiques** restent sur Hugo (voir ci-dessous).
- Effet de bord à connaître : les constantes `ATTACK_DAMAGE`, `FIRE_DELAY`, `ATTACK_RANGE` de `Player` passent à 1. Les mobs ne sont pas touchés (leurs stats restent absolues dans `Creature`).
- Les bonus de Joël (tabac, maté, easter egg Goku) utilisent déjà `addMultiplier` / `removeMultiplier` sur ces stats : ils continuent de marcher, pour toutes les armes.
- `EcranCarte.trouverMobLePlusProche` lit aujourd'hui `hugo.getAttackRange().getValue()` comme une distance : il doit lire la **portée réelle** via une nouvelle méthode `player.getShootRange()` (portée de l'arme × multiplicateur). Même chose partout où une valeur absolue était lue.
- **Critique** — *choix de Claude* : tiré **une fois par tir** dans `Player.shoot`, puis appliqué à toutes les balles de ce tir (les 5 plombs d'un même coup sont tous critiques ou aucun). Plus simple, et un critique au fusil à pompe se sent.

### Chiffres de départ — *proposition de Claude, à régler en jouant*

Mobs pour repère : 10, 7, 40, 80, 250 PV.

| Arme | Dégâts / balle | Délai | Portée | Balles par tir | Particularité | Dégâts/s max (sans amélioration) |
|---|---|---|---|---|---|---|
| Carabine | 8 | 1,0 s | 500 | 1 | — | 8 |
| PM | 3 | 0,2 s | 350 | 1 | — | 15 |
| Fusil à pompe | 6 | 1,4 s | 260 | 5 | éventail de 40° | 21 (si tous les plombs touchent) |
| Mitrailleuse | 12 | 0,4 s | 600 | 1 | rare | 30 |
| Lance-flammes | 3 | 0,1 s | 180 | 1 | perçant, flamme large, léger écart aléatoire | 30 sur chaque mob dans la zone |

Chaque arme a un défaut net : le PM tire de près et faiblement, le fusil à pompe de très près et lentement, le lance-flammes de tout près, la mitrailleuse est rare.

### `Projectile` : balles perçantes

- Nouvel attribut `piercing` (faux par défaut) via un **nouveau constructeur** ; les constructeurs actuels (utilisés par Joël) ne changent pas.
- Une balle perçante n'est **pas détruite** quand elle touche un mob, et ne touche **chaque mob qu'une fois** (elle retient les mobs déjà touchés), sinon une flamme toucherait le même mob 60 fois par seconde.
- `verifierTouches` dans `EcranCarte` : `destroy()` seulement si la balle n'est pas perçante.

### `Player`

- Attribut `Weapon weapon`, initialisé à la carabine.
- `canShoot()` compare le chrono au délai **de l'arme** × multiplicateur de Hugo.
- `shoot(...)` devient `List<Projectile> shoot(Creature target)` : calcule les dégâts (avec critique), puis délègue à `weapon.fire(...)`.
- `equipWeapon(Weapon w)` : change d'arme et remet le chrono de tir à zéro ; `getWeapon()` pour l'affichage.

### Obtenir une arme : `WeaponUpgrade`

- Nouvelle carte `WeaponUpgrade extends Upgrade`, dont `apply` appelle `player.equipWeapon(...)`.
- Une carte par arme dans `UpgradePool` (sauf la carabine, déjà en main).
- **Ne pas proposer l'arme déjà équipée** : `UpgradePool.pickRandom` reçoit le joueur et écarte les cartes qui ne s'appliquent pas.
- **Rareté** : chaque `Upgrade` a un **poids** (1 par défaut, via un constructeur supplémentaire : les cartes existantes ne changent pas) ; `pickRandom` tire 3 cartes **différentes** au hasard en tenant compte des poids. Proposition : mitrailleuse 0,3, autres armes 0,7, cartes de stats 1.
- Coffres : Joël peut appeler `hugo.equipWeapon(Weapons.shotgun(...))` — rien à faire de notre côté.

### Images — *choix de Claude*

- **Cartes** : 4 nouvelles caisses (PM, fusil à pompe, mitrailleuse, lance-flammes), normale + survolée, même style que les autres, avec l'icône de l'arme tirée du Drive (dossier « Arme ») à la place du pictogramme. Étiquette « ARME » au lieu de « STAT ».
- **Balles** : la balle cuivre actuelle de Joël pour carabine, PM et mitrailleuse ; plombs = petite balle ; flammes = sprite orange simple (ou pixel teinté) — laissé au moment de la construction.

## Laissé à la construction

- Le dessin exact des flammes et des plombs.
- Le réglage fin des chiffres (en jouant).
- Le texte exact des cartes d'armes.

## Comment on construit

Pas à pas avec Thomas, en le guidant par des questions (comme pour `Life`, `Stat` et les menus), dans une branche `weapons`, une étape testable à la fois : d'abord la carabine dans `Weapon` sans rien changer au jeu, puis les autres armes, puis les cartes.
