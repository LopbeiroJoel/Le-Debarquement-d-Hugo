//gère l’écran le regard de la map // les touches/caméra/zoom
package hugo.map;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

import hugo.entity.Player;
import hugo.entity.Projectile;
import hugo.mob.Mob;
import hugo.mob.spawn.MobSpawner;
import hugo.wave.WaveManager;

public final class EcranCarte extends ScreenAdapter {

    private static final double VITESSE_CAMERA = 420;
    private static final double ZOOM_INITIAL = 1.25; // zoom initial de la caméra

    private final boolean modeVerification;

    private CarteJeu carte;
    private RenduCarte rendu;

    private SpriteBatch dessin;
    private BitmapFont police;
    private Texture pixelBlanc;
    private Texture textureHugo;
    private Player hugo;
    private final List<Projectile> projectiles = new ArrayList<>();
    private TextureRegion bulletSprite;

    private final WaveManager waveManager = new WaveManager();
    private final MobSpawner mobSpawner = new MobSpawner();

    private final CameraCarte camera = new CameraCarte();

    private final OrthographicCamera cameraMonde =
        new OrthographicCamera();

    private final OrthographicCamera cameraInterface =
        new OrthographicCamera();

    private double positionX = CarteJeu.SPAWN_X;
    private double positionY = CarteJeu.SPAWN_Y;

    private boolean afficherCollisions = false;
    private boolean afficherPetiteCarte = true;

    private int nombreImages = 0;

    private InputAdapter commandesSouris;

    private static final float XP_PAR_MOB = 10;

    public EcranCarte(boolean modeVerification) {
        this.modeVerification = modeVerification;
    }

    public void show() {
        if (carte != null) {
            Gdx.input.setInputProcessor(commandesSouris);
            return;
        }

        chargerCarte();
        preparerDessin();
        mobSpawner.spawnWave(waveManager, carte, hugo.getX(), hugo.getY());
        preparerSouris();
    }

    private void chargerCarte() {
        try {
            carte = new CarteJeu();
        } catch (Exception erreur) {
            throw new IllegalStateException(
                "Chargement de la map impossible",
                erreur
            );
        }
    }

    private void preparerDessin() {
        dessin = new SpriteBatch();
        rendu = new RenduCarte(carte);
        police = new BitmapFont();
        textureHugo = new Texture("hero-v2.png");
        hugo = new Player(textureHugo, CarteJeu.SPAWN_X, CarteJeu.SPAWN_Y);
        hugo.setMap(carte);


        Pixmap image = new Pixmap(
            1,
            1,
            Pixmap.Format.RGBA8888
        );

        image.setColor(Color.WHITE);
        image.fill();

        pixelBlanc = new Texture(image);
        bulletSprite = new TextureRegion(pixelBlanc);

        image.dispose();
    }

    private void preparerSouris() {
        commandesSouris = new InputAdapter() {  //zoom molette
            public boolean scrolled(float horizontal, float vertical) {
                camera.zoom = camera.zoom - vertical * 0.1;

                return true;
            }
        };

        Gdx.input.setInputProcessor(commandesSouris);
    }

    public void render(float delta) {
        int largeur = Gdx.graphics.getWidth();
        int hauteur = Gdx.graphics.getHeight();

        if (largeur <= 0 || hauteur <= 0) {
            return;
        }

        if (modeVerification) {
            positionnerCameraVerification();
        }

        double tempsEcoule = Math.min(delta, 0.05);

        deplacerCamera(tempsEcoule);
        lireRaccourcis();
        actualiserCamera(largeur, hauteur);

        for (Mob mob : mobSpawner.getMobs()) {
            mob.updateAgainst(hugo, carte, (float) tempsEcoule, mobSpawner);
        }

        mobSpawner.updateWave(
            waveManager,
            delta,
            carte,
            hugo.getX(),
            hugo.getY()
        );
        afficherMonde();
        afficherInterface(largeur, hauteur);

        if (modeVerification) {
            verifierAffichage();
        }
    }

    private void deplacerCamera(double tempsEcoule) { // caméra touches / fleches
        double directionX = 0;
        double directionY = 0;

        if (toucheMaintenue(Input.Keys.D, Input.Keys.RIGHT)) {
            directionX = directionX + 1;
        }

        if (
            toucheMaintenue(
                Input.Keys.Q,
                Input.Keys.A,
                Input.Keys.LEFT
            )
        ) {
            directionX = directionX - 1;
        }

        if (toucheMaintenue(Input.Keys.S, Input.Keys.DOWN)) {
            directionY = directionY + 1;
        }
        if (
            toucheMaintenue(
                Input.Keys.Z,
                Input.Keys.W,
                Input.Keys.UP
            )
        ) {
            directionY = directionY - 1;
        }

        double longueur = Math.hypot(directionX, directionY);

        if (longueur > 0) {
            directionX = directionX / longueur;
            directionY = directionY / longueur;
        }

        hugo.setDirection((float) directionX, (float) directionY);  
        hugo.update((float) tempsEcoule);         

        if (hugo.canShoot()) {
              Mob cible = trouverMobLePlusProche();
            if (cible != null) {                                   
                Projectile projectile = hugo.shoot(bulletSprite, cible); 
                projectile.setMap(carte);
                projectiles.add(projectile);
            }
        }

        for (Projectile projectile : projectiles) {
            projectile.update((float) tempsEcoule);
        }
        verifierTouches();
        projectiles.removeIf(projectile -> !projectile.isActive());
        mobSpawner.getMobs().removeIf(mob -> mob.getLife().isDead());

        positionX = hugo.getX();               
        positionY = hugo.getCenterY();
    }

    private void verifierTouches() {
        for (Projectile projectile : projectiles) {
            for (Mob mob : mobSpawner.getMobs()) {
                if (!projectile.isActive() || mob.getLife().isDead()) {
                    continue;
                }
                if (projectile.getHitbox().overlaps(mob.getHitbox())) {
                    mob.getLife().takeDamage(projectile.getDamage());   
                    projectile.destroy();
                    if (mob.getLife().isDead()) {
                        hugo.addXp(XP_PAR_MOB);
                    }                          
                }
            }
        }
    }

    private Mob trouverMobLePlusProche() {
        Mob plusProche = null;                       
        float meilleureDistance = Float.MAX_VALUE;     
        float portee = hugo.getAttackRange().getValue();

        for (Mob mob : mobSpawner.getMobs()) {
            if (mob.getLife().isDead()) {
                continue;
            }
            float dx = mob.getX() - hugo.getX();                 
            float dy = mob.getCenterY() - hugo.getCenterY();      
            float distance = (float) Math.sqrt(dx * dx + dy * dy);  

            if (distance <= portee && distance < meilleureDistance) {              
                meilleureDistance = distance;
                plusProche = mob;
            }
        }
        return plusProche;                            
    }

    private boolean toucheMaintenue(int... touches) {
        for (int touche : touches) {
            if (Gdx.input.isKeyPressed(touche)) {
                return true;
            }
        }

        return false;
    }

    private void lireRaccourcis() { //zoom avec clavier
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            afficherCollisions = !afficherCollisions;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            afficherPetiteCarte = !afficherPetiteCarte;
        }

        boolean agrandir =
            Gdx.input.isKeyJustPressed(Input.Keys.PLUS) ||
            Gdx.input.isKeyJustPressed(Input.Keys.EQUALS) ||
            Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ADD);

        if (agrandir) {
            camera.zoom = camera.zoom + 0.15;
        }

        boolean reduire =
            Gdx.input.isKeyJustPressed(Input.Keys.MINUS) ||
            Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_SUBTRACT);

        if (reduire) {
            camera.zoom = camera.zoom - 0.15;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            revenirAuDepart();
        }
    }

    private void revenirAuDepart() {
        positionX = CarteJeu.SPAWN_X;
        positionY = CarteJeu.SPAWN_Y;

        camera.zoom = ZOOM_INITIAL;

        carte.reset();
    }

    private void actualiserCamera(int largeur, int hauteur) {
        camera.follow(
            positionX,
            positionY,
            largeur,
            hauteur,
            carte.width,
            carte.height
        );

        positionX = camera.x;
        positionY = camera.y;

        cameraMonde.setToOrtho(true, largeur, hauteur);

        cameraMonde.zoom = (float) (1 / camera.zoom);

        cameraMonde.position.set(
            (float) camera.x,
            (float) camera.y,
            0
        );

        cameraMonde.update();
    }

    private void afficherMonde() {
        ScreenUtils.clear(0.04f, 0.07f, 0.05f, 1);

        rendu.draw(dessin, cameraMonde, hugo, mobSpawner.getMobs());

        dessin.setProjectionMatrix(cameraMonde.combined);
        dessin.begin();
        for (Projectile projectile : projectiles) {
            projectile.draw(dessin);
        }
        dessin.end();

        if (afficherCollisions) {
            rendu.drawCollisions(cameraMonde);
        }
    }

    private void afficherInterface(int largeur, int hauteur) {
        cameraInterface.setToOrtho(false, largeur, hauteur);

        dessin.setProjectionMatrix(cameraInterface.combined);
        dessin.begin();

       

        if (afficherPetiteCarte) {
            dessinerPetiteCarte(largeur, hauteur);
        }

        afficherAide();

        if (afficherCollisions) {
            afficherInformationsDebug(hauteur);
        }

        dessin.setColor(Color.WHITE);
        dessin.end();
    }

    private void afficherTitre(int hauteur) {
        dessinerRectangle(
            16,
            hauteur - 48,
            254,
            30,
            0.04f,
            0.07f,
            0.05f,
            0.72f
        );

        police.setColor(0.88f, 0.84f, 0.67f, 1);

        police.draw(
            dessin,
            "Pour mettre un futur titre à voir",
            28,
            hauteur - 28
        );
    }

    private void dessinerPetiteCarte(int largeur, int hauteur) { //petite carte
        float x = largeur - 254;
        float y = hauteur - 184;

        dessinerRectangle(
            x - 7,
            y - 7,
            244,
            174,
            0.04f,
            0.07f,
            0.05f,
            0.85f
        );

        dessin.setColor(Color.WHITE);

        dessin.draw(
            rendu.overview,
            x,
            y,
            230,
            160
        );

        dessinerCadreCamera(x, y, largeur, hauteur);
    }

    private void dessinerCadreCamera(
        float xPetiteCarte,
        float yPetiteCarte,
        int largeurEcran,
        int hauteurEcran
    ) {
        double largeurVisible = largeurEcran / camera.zoom;
        double hauteurVisible = hauteurEcran / camera.zoom;

        double gaucheVisible = camera.x - largeurVisible / 2; //curseur minimap
        double basVisible = camera.y + hauteurVisible / 2;

        float gauche = xPetiteCarte  // position de la caméra sur la petite carte
            + (float) (gaucheVisible / carte.width * 230);

        float bas = yPetiteCarte + 160
            - (float) (basVisible / carte.height * 160);

        float largeurCadre =
            (float) (largeurVisible / carte.width * 230);

        float hauteurCadre =
            (float) (hauteurVisible / carte.height * 160);

        dessin.setColor(1, 1, 0.85f, 0.8f); //curseur couleur/direction ou il va

        dessin.draw(
            pixelBlanc,
            gauche,
            bas,
            largeurCadre,
            1
        );

        dessin.draw(
            pixelBlanc,
            gauche,
            bas + hauteurCadre,
            largeurCadre,
            1
        );

        dessin.draw(
            pixelBlanc,
            gauche,
            bas,
            1,
            hauteurCadre
        );

        dessin.draw(
            pixelBlanc,
            gauche + largeurCadre,
            bas,
            1,
            hauteurCadre
        );

        dessin.setColor(Color.WHITE);
    }

    private void afficherAide() {
        dessinerRectangle(
            20,
            17,
            478,
            27,
            0.04f,
            0.07f,
            0.05f,
            0.72f
        );

        police.setColor(0.9f, 0.9f, 0.8f, 1);

        police.draw(
            dessin,
            "ZQSD / Fleches : camera libre    Molette : zoom    M : carte",
            30,
            35
        );
    }

    private void afficherInformationsDebug(int hauteur) {
        int x = (int) camera.x;
        int y = (int) camera.y;
        int imagesParSeconde = Gdx.graphics.getFramesPerSecond();

        String informations =
            carte.entityCount() + " objets | "
            + x + ", " + y + " | "
            + imagesParSeconde + " FPS";

        police.draw(
            dessin,
            informations,
            28,
            hauteur - 70
        );
    }

    private void dessinerRectangle(
        float x,
        float y,
        float largeur,
        float hauteur,
        float rouge,
        float vert,
        float bleu,
        float transparence
    ) {
        dessin.setColor(
            rouge,
            vert,
            bleu,
            transparence
        );

        dessin.draw(
            pixelBlanc,
            x,
            y,
            largeur,
            hauteur
        );

        dessin.setColor(Color.WHITE);
    }

    private void positionnerCameraVerification() {
        if (nombreImages == 30) {
            positionX = 3100;
            positionY = 850;
        }

        if (nombreImages == 60) {
            positionX = 3900;
            positionY = 1850;
        }
    }

    private void verifierAffichage() {
        nombreImages = nombreImages + 1;

        boolean enregistrer =
            nombreImages == 30 ||
            nombreImages == 60 ||
            nombreImages == 90;

        if (!enregistrer) {
            return;
        }

        enregistrerCapture();

        Gdx.app.log(
            "Verification",
            "libGDX : "
                + carte.entityCount()
                + " objets, rendu OpenGL, "
                + nombreImages
                + " images sans erreur"
        );

        if (nombreImages == 90) {
            Gdx.app.exit();
        }
    }

    private void enregistrerCapture() {
        String nomFichier = "libgdx-smoke.png";

        if (nombreImages == 60) {
            nomFichier = "libgdx-base.png";
        }

        if (nombreImages == 90) {
            nomFichier = "libgdx-tranchees.png";
        }

        String dossierParDefaut =
            System.getProperty("java.io.tmpdir")
                + "/hugo-map-check";

        String dossier = System.getProperty(
            "hugo.smokeDir",
            dossierParDefaut
        );

        Pixmap capture = Pixmap.createFromFrameBuffer(
            0,
            0,
            Gdx.graphics.getBackBufferWidth(),
            Gdx.graphics.getBackBufferHeight()
        );

        try {
            PixmapIO.writePNG(
                Gdx.files.absolute(dossier + "/" + nomFichier),
                capture,
                -1,
                true
            );
        } finally {
            capture.dispose();
        }
    }

    public void hide() {
        if (Gdx.input.getInputProcessor() == commandesSouris) {
            Gdx.input.setInputProcessor(null);
        }
    }

    public void dispose() {
        hide();

        if (rendu != null) {
            rendu.dispose();
        }

        if (dessin != null) {
            dessin.dispose();
        }

        if (police != null) {
            police.dispose();
        }

        mobSpawner.dispose();

        if (pixelBlanc != null) {
            pixelBlanc.dispose();
        }
        if (textureHugo != null) {
            textureHugo.dispose();
        }
    }
}
