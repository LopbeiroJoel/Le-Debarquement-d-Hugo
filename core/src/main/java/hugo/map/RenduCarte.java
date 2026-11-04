//placement de couche map : sol/objet sol/ombres/objets
package hugo.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public final class RenduCarte implements Disposable {

    private final CarteJeu carte;
    private final ImagesDecor images = new ImagesDecor();

    // les images du sol
    private final List<TextureRegion> morceauxSol = new ArrayList<>();

    // pour les traits rouges avec F1
    private final ShapeRenderer contours = new ShapeRenderer();

    // ce qu'on voit à l'écran
    private final Rectangle zoneVisible = new Rectangle();

    private final FrameBuffer imagePetiteCarte;
    public final TextureRegion overview;

    public RenduCarte(CarteJeu carte) {
        this.carte = carte;

        chargerSol();
        chargerDecor();

        imagePetiteCarte = creerPetiteCarte();

        overview = new TextureRegion(
            imagePetiteCarte.getColorBufferTexture()
        );

        overview.flip(false, true);
    }

    // charge le sol morceau par morceau
    private void chargerSol() {
        for (int y = 0; y < carte.height; y = y + 1024) {
            for (int x = 0; x < carte.width; x = x + 1024) {
                String chemin =
                    "render/ground/" + x + "-" + y + ".png";

                Texture texture = new Texture(
                    Gdx.files.internal(chemin)
                );

                // garde les pixels nets
                texture.setFilter(
                    Texture.TextureFilter.Nearest,
                    Texture.TextureFilter.Nearest
                );

                TextureRegion morceau = new TextureRegion(texture);

                morceau.flip(false, true);

                morceauxSol.add(morceau);
            }
        }
    }

    // charge les arbres, maisons, barrières...
    private void chargerDecor() {
        for (TypeDecor type : TypeDecor.values()) {
            images.get(type);
        }
    }

    // fait une image de toute la map pour la mini-map
    private FrameBuffer creerPetiteCarte() {
        FrameBuffer image = new FrameBuffer(
            Pixmap.Format.RGBA8888,
            460,
            320,
            false
        );

        OrthographicCamera camera = new OrthographicCamera();

        camera.setToOrtho(true, carte.width, carte.height);

        SpriteBatch dessin = new SpriteBatch();

        image.begin();

        ScreenUtils.clear(0, 0, 0, 1);

        draw(dessin, camera, 0, () -> {});

        image.end();

        dessin.dispose();

        return image;
    }

    // dessine la map
    public void draw(
        SpriteBatch dessin,
        OrthographicCamera camera,
        float personnageY,
        Runnable dessinerPersonnage
    ) {
        actualiserZoneVisible(camera);

        dessin.setProjectionMatrix(camera.combined);
        dessin.begin();

        // sol : on marche dessus
        dessinerSol(dessin);

        // cratères et trucs au sol
        dessinerElementsAuSol(dessin);

        // ombres
        dessinerOmbres(dessin);

        // arbres, maisons, barrières et perso
        dessinerDecorEtPersonnage(
            dessin,
            personnageY,
            dessinerPersonnage
        );

        dessin.end();
    }

    // regarde quelle partie de la map est à l'écran
    private void actualiserZoneVisible(OrthographicCamera camera) {
        float largeur = camera.viewportWidth * camera.zoom;
        float hauteur = camera.viewportHeight * camera.zoom;

        float gauche = camera.position.x - largeur / 2;
        float haut = camera.position.y - hauteur / 2;

        zoneVisible.set(gauche, haut, largeur, hauteur);
    }

    // première couche : herbe, terre, routes
    private void dessinerSol(SpriteBatch dessin) {
        int numeroMorceau = 0;

        for (int y = 0; y < carte.height; y = y + 1024) {
            for (int x = 0; x < carte.width; x = x + 1024) {
                TextureRegion morceau = morceauxSol.get(numeroMorceau);

                numeroMorceau = numeroMorceau + 1;

                Rectangle position = new Rectangle(
                    x,
                    y,
                    morceau.getRegionWidth(),
                    morceau.getRegionHeight()
                );

                // dessine seulement si on le voit
                if (zoneVisible.overlaps(position)) {
                    dessin.draw(morceau, x, y);
                }
            }
        }
    }

    // les détails posés sur le sol
    private void dessinerElementsAuSol(SpriteBatch dessin) {
        for (ObjetDecor objet : carte.getEntities()) {
            if (objet.ground && estVisible(objet)) {
                dessinerObjet(dessin, objet);
            }
        }
    }

    // ombres des objets
    private void dessinerOmbres(SpriteBatch dessin) {
        for (ObjetDecor objet : carte.getEntities()) {
            if (!objet.ground && estVisible(objet)) {
                dessinerOmbre(dessin, objet);
            }
        }
    }

    private void dessinerOmbre(
        SpriteBatch dessin,
        ObjetDecor objet
    ) {
        TypeDecor type = objet.getType();

        ImagesDecor.GroundShadow ombre = images.shadow(type);

        float gauche = objet.x - type.width / 2f;
        float haut = objet.y - type.height;

        float transparence = 0.25f;

        // ombre un peu plus légère pour les arbres
        if (type.footprint.equals("tree")) {
            transparence = 0.20f;
        }

        // ombre qui part sur le côté
        dessin.setColor(0.10f, 0.13f, 0.09f, transparence);

        dessin.draw(
            ombre.cast,
            gauche,
            haut,
            ombre.width,
            ombre.height
        );

        // ombre au pied de l'objet
        dessin.setColor(0.07f, 0.09f, 0.055f, 0.38f);

        dessin.draw(
            ombre.contact,
            gauche,
            haut,
            ombre.width,
            ombre.height
        );

        dessin.setColor(Color.WHITE);
    }

    // met le perso devant ou derrière les objets
    private void dessinerDecorEtPersonnage(
        SpriteBatch dessin,
        float personnageY,
        Runnable dessinerPersonnage
    ) {
        boolean personnageDessine = false;

        dessin.setColor(Color.WHITE);

        for (ObjetDecor objet : carte.getEntities()) {
            // le sol est déjà dessiné
            if (objet.ground) {
                continue;
            }

            // le perso est derrière cet objet
            if (!personnageDessine && personnageY < objet.y) {
                dessinerPersonnage.run();
                personnageDessine = true;
            }

            // arbre, maison, barrière...
            if (estVisible(objet)) {
                dessinerObjet(dessin, objet);
            }
        }

        // sinon le perso est devant tous les objets
        if (!personnageDessine) {
            dessinerPersonnage.run();
        }
    }

    // dessine l'image d'un objet à sa place
    private void dessinerObjet(
        SpriteBatch dessin,
        ObjetDecor objet
    ) {
        TypeDecor type = objet.getType();

        TextureRegion image = images.get(type);

        float gauche = objet.x - type.width / 2f;
        float haut = objet.y - type.height;

        dessin.draw(
            image,
            gauche,
            haut,
            type.width,
            type.height
        );
    }

    // vérifie si l'objet est dans l'écran
    private boolean estVisible(ObjetDecor objet) {
        TypeDecor type = objet.getType();

        float gauche = objet.x - type.width / 2f;
        float haut = objet.y - type.height;

        // garde un peu de place pour son ombre
        float largeur = type.width + type.height * 0.25f;
        float hauteur = type.height + type.height * 0.35f;

        Rectangle position = new Rectangle(
            gauche,
            haut,
            largeur,
            hauteur
        );

        return zoneVisible.overlaps(position);
    }

    // F1 : montre les collisions en rouge
    // le blocage du perso se fait dans CarteJeu
    public void drawCollisions(OrthographicCamera camera) {
        contours.setProjectionMatrix(camera.combined);

        contours.begin(ShapeRenderer.ShapeType.Line);
        contours.setColor(1, 0.25f, 0.2f, 1);

        // arbres, maisons, barrières : le perso ne traverse pas leur collision
        for (ObjetDecor objet : carte.getEntities()) {
            if (objet.collision != null && estVisible(objet)) {
                dessinerRectangleCollision(objet.collision);
            }
        }

        // murs des tranchées
        for (Rectangle mur : carte.trenchWalls) {
            dessinerRectangleCollision(mur);
        }

        // bord de la rivière
        ZoneCarte riviere = carte.areas.get("riverShape");

        for (float[] contour : riviere.polygons) {
            contours.polygon(contour);
        }

        contours.end();
    }

    // dessine un rectangle rouge
    private void dessinerRectangleCollision(Rectangle rectangle) {
        contours.rect(
            rectangle.x,
            rectangle.y,
            rectangle.width,
            rectangle.height
        );
    }

    // libère les images quand on ferme
    public void dispose() {
        for (TextureRegion morceau : morceauxSol) {
            morceau.getTexture().dispose();
        }

        images.dispose();
        imagePetiteCarte.dispose();
        contours.dispose();
    }
}
