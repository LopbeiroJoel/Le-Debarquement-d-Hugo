//Gere les colisions et l'ouverture des coffres

package hugo.map;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class CarteJeu {

    public static final float SPAWN_X = 2200;
    public static final float SPAWN_Y = 1500;

    public static final float WATER_TOP = 2330;
    public static final float WATER_BOTTOM = 2510;

    public static final int[] BRIDGES = {1250, 3420};

    private static final int TAILLE_CASE = 128;
    private static final int COLONNES_GRILLE = 64;

    public final int width;
    public final int height;

    private final List<ObjetDecor> entities = new ArrayList<>();
    private final List<ObjetDecor> chests = new ArrayList<>();

    private final Map<Integer, List<ObjetDecor>> cells = new HashMap<>();

    public final List<Rectangle> trenchWalls = new ArrayList<>();

    public final Map<String, ZoneCarte> areas = new HashMap<>();

    private Consumer<String> chestListener = identifiant -> {};

    public CarteJeu() throws IOException {
        JsonValue donnees = lireFichierCarte();

        width = donnees.getInt("width");
        height = donnees.getInt("height");

        chargerZones(donnees);
        chargerObjets(donnees);
        ajouterMursTranchees();

        Rectangle positionDepart = playerBox(SPAWN_X, SPAWN_Y);

        if (!canMove(positionDepart)) {
            throw new IOException("Depart bloque");
        }
    }

    private JsonValue lireFichierCarte() throws IOException {
        try (
            InputStream fichier = CarteJeu.class.getResourceAsStream(
                "/maps/carte.json"
            )
        ) {
            if (fichier == null) {
                throw new IOException("Map introuvable");
            }

            JsonReader lecteur = new JsonReader();

            return lecteur.parse(fichier);
        }
    }

    private void chargerZones(JsonValue donnees) {
        JsonValue zone = donnees.get("areas").child;

        while (zone != null) {
            ZoneCarte nouvelleZone = new ZoneCarte(zone);

            areas.put(zone.name, nouvelleZone);

            zone = zone.next;
        }
    }

    private void chargerObjets(JsonValue donnees) {
        JsonValue objet = donnees.get("entities").child;

        while (objet != null) {
            String nomType = objet.getString("type");

            TypeDecor type = TypeDecor.valueOf(nomType);

            float x = objet.getFloat("x");
            float y = objet.getFloat("y");

            add(type, x, y);

            objet = objet.next;
        }
    }

    private void ajouterMursTranchees() {
        trenchWalls.add(new Rectangle(3330, 1620, 870, 18));
        trenchWalls.add(new Rectangle(3330, 1683, 830, 18));

        trenchWalls.add(new Rectangle(3510, 1810, 650, 18));
        trenchWalls.add(new Rectangle(3570, 1873, 590, 18));

        trenchWalls.add(new Rectangle(3570, 2010, 750, 18));
        trenchWalls.add(new Rectangle(3510, 2073, 810, 18));
    }

    public void add(TypeDecor type, float x, float y) {
        String identifiant = type.name() + "_" + entities.size();

        ObjetDecor objet = new ObjetDecor(identifiant, type, x, y);

        entities.add(objet);
        if (objet.isChest()) {
            chests.add(objet);
        }

        entities.sort(
            Comparator.comparingDouble(element -> element.y)
        );

        if (objet.collision != null) {
            rangerCollision(objet);
        }
    }

    private void rangerCollision(ObjetDecor objet) {
        Rectangle collision = objet.collision;

        int premiereColonne = (int) collision.x / TAILLE_CASE;

        int derniereColonne =
            (int) (collision.x + collision.width) / TAILLE_CASE;

        int premiereLigne = (int) collision.y / TAILLE_CASE;

        int derniereLigne =
            (int) (collision.y + collision.height) / TAILLE_CASE;

        for (int ligne = premiereLigne; ligne <= derniereLigne; ligne++) {
            for (
                int colonne = premiereColonne;
                colonne <= derniereColonne;
                colonne++
            ) {
                int numeroCase = ligne * COLONNES_GRILLE + colonne;

                if (!cells.containsKey(numeroCase)) {
                    cells.put(numeroCase, new ArrayList<>());
                }

                List<ObjetDecor> objetsCase = cells.get(numeroCase);

                objetsCase.add(objet);
            }
        }
    }

    public int entityCount() {
        return entities.size();
    }

    public List<ObjetDecor> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    public boolean overlapsRoad(ObjetDecor objet) {
        TypeDecor type = objet.getType();

        float gauche = objet.x - type.width / 2f;
        float haut = objet.y - type.height;

        Rectangle imageObjet = new Rectangle(
            gauche,
            haut,
            type.width,
            type.height
        );

        ZoneCarte routes = areas.get("roads");

        return routes.intersects(imageObjet);
    }

    public static Rectangle playerBox(float x, float piedsY) {
        return new Rectangle(x - 11, piedsY - 17, 22, 17);
    }

    public boolean canMove(Rectangle position) { //LES COLISIONS VIA LE GET ! C'EST ICI !!!!
        if (depasseLimites(position)) {
            return false;
        }

        ZoneCarte riviere = areas.get("riverShape");

        if (!estSurPont(position) && riviere.intersects(position)) {
            return false;
        }

        for (Rectangle mur : trenchWalls) {
            if (mur.overlaps(position)) {
                return false;
            }
        }

        if (toucheDecor(position)) {
            return false;
        }

        return true;
    }

    private boolean depasseLimites(Rectangle position) {
        if (position.x < 0) {
            return true;
        }

        if (position.y < 0) {
            return true;
        }

        if (position.x + position.width > width) {
            return true;
        }

        if (position.y + position.height > height) {
            return true;
        }

        return false;
    }

    private boolean estSurPont(Rectangle position) {
        float gaucheJoueur = position.x;
        float droiteJoueur = position.x + position.width;

        for (int centrePont : BRIDGES) {
            float gauchePont = centrePont - 60;
            float droitePont = centrePont + 60;

            if (
                gaucheJoueur >= gauchePont &&
                droiteJoueur <= droitePont
            ) {
                return true;
            }
        }

        return false;
    }

    private boolean toucheDecor(Rectangle position) {
        int premiereColonne = (int) position.x / TAILLE_CASE;

        int derniereColonne =
            (int) (position.x + position.width) / TAILLE_CASE;

        int premiereLigne = (int) position.y / TAILLE_CASE;

        int derniereLigne =
            (int) (position.y + position.height) / TAILLE_CASE;

        for (int ligne = premiereLigne; ligne <= derniereLigne; ligne++) {
            for (
                int colonne = premiereColonne;
                colonne <= derniereColonne;
                colonne++
            ) {
                int numeroCase = ligne * COLONNES_GRILLE + colonne;

                List<ObjetDecor> objetsCase = cells.get(numeroCase);

                if (objetsCase != null) {
                    for (ObjetDecor objet : objetsCase) {
                        if (objet.collision.overlaps(position)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public Vector2 move(float x, float y, float dx, float dy) {
        float distanceHorizontale = Math.abs(dx);
        float distanceVerticale = Math.abs(dy);

        float plusGrandeDistance = Math.max(
            distanceHorizontale,
            distanceVerticale
        );

        int nombrePas = (int) Math.ceil(plusGrandeDistance / 4);

        if (nombrePas < 1) {
            nombrePas = 1;
        }

        float petitPasX = dx / nombrePas;
        float petitPasY = dy / nombrePas;

        for (int pas = 0; pas < nombrePas; pas++) {
            Rectangle prochainePositionX = playerBox(x + petitPasX, y);

            if (canMove(prochainePositionX)) {
                x = x + petitPasX;
            }

            Rectangle prochainePositionY = playerBox(x, y + petitPasY);

            if (canMove(prochainePositionY)) {
                y = y + petitPasY;
            }
        }

        return new Vector2(x, y);
    }

    public boolean blocksShot(
        float departX,
        float departY,
        float arriveeX,
        float arriveeY
    ) {
        for (Rectangle mur : trenchWalls) {
            boolean toucheMur = ZoneCarte.segment(
                mur,
                departX,
                departY,
                arriveeX,
                arriveeY
            );

            if (toucheMur) {
                return true;
            }
        }

        for (ObjetDecor objet : entities) {
            if (objet.collision != null) {
                boolean toucheObjet = ZoneCarte.segment(
                    objet.collision,
                    departX,
                    departY,
                    arriveeX,
                    arriveeY
                );

                if (toucheObjet) {
                    return true;
                }
            }
        }

        return false;
    }

    public void setChestListener(Consumer<String> action) {
        if (action == null) {
            chestListener = identifiant -> {};
        } else {
            chestListener = action;
        }
    }

    public void updateTriggers(float x, float y) {
        for (ObjetDecor chest : chests) {
            chest.checkChestTrigger(Vector2.dst2(x, y, chest.x, chest.y) < 68f * 68f);
        }
    }

    public void updateChests(float delta) {
        for (ObjetDecor chest : chests) {
            if (chest.updateChest(delta)) {
                chestListener.accept(chest.id);
            }
        }
    }

    public void reset() {
        for (ObjetDecor chest : chests) {
            chest.reset();
        }
    }

    public String zoneAt(float x, float y) {
        if (y > WATER_TOP - 80 && y < WATER_BOTTOM + 100) {
            return "RIVIERE";
        }

        if (areas.get("base").contains(x, y)) {
            return "BASE MILITAIRE";
        }

        if (areas.get("trenches").contains(x, y)) {
            return "LES TRANCHEES";
        }

        if (areas.get("village").contains(x, y)) {
            return "VILLAGE EN RUINE";
        }

        if (areas.get("forest").contains(x, y)) {
            return "FORET DU NORD";
        }

        if (y > WATER_BOTTOM) {
            return "LE SUD DE LA MAP";
        }

        return "LE BOCAGE";
    }
}
