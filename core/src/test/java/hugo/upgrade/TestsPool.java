package hugo.upgrade;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import hugo.entity.Player;

public final class TestsPool {

    private static void check(boolean ok, String text) {
        if (!ok) throw new AssertionError(text);
    }

    // une fausse carte pour les tests : elle ne fait rien quand on la choisit
    private static Upgrade card(String name, float weight) {
        return new Upgrade(name, name, weight) {
            @Override
            public void apply(Player player) {
            }
        };
    }

    public static void main(String[] args) {
        Random rng = new Random(42);   // graine fixe : le hasard est le meme a chaque lancement

        List<Upgrade> cards = new ArrayList<>();
        cards.add(card("a", 1));
        cards.add(card("b", 1));
        cards.add(card("c", 1));
        cards.add(card("d", 1));
        cards.add(card("rare", 0.3f));
        cards.add(card("never", 0));

        for (int i = 0; i < 1000; i++) {
            List<Upgrade> picked = UpgradePool.pickRandom(cards, 3, u -> true, rng);
            check(picked.size() == 3, "3 cartes tirees");
            check(new HashSet<>(picked).size() == 3, "jamais deux fois la meme carte");
            for (Upgrade u : picked) {
                check(!u.getName().equals("never"), "un poids 0 n'est jamais tire");
            }
        }

        for (int i = 0; i < 1000; i++) {
            List<Upgrade> picked = UpgradePool.pickRandom(cards, 3, u -> !u.getName().equals("a"), rng);
            for (Upgrade u : picked) {
                check(!u.getName().equals("a"), "une carte indisponible n'est jamais tiree");
            }
        }

        List<Upgrade> few = UpgradePool.pickRandom(cards, 3,
                u -> u.getName().equals("a") || u.getName().equals("b"), rng);
        check(few.size() == 2, "seulement 2 cartes disponibles : on en renvoie 2");

        List<Upgrade> duo = new ArrayList<>();
        duo.add(card("speed", 1));
        duo.add(card("rare", 0.3f));
        int rare = 0;
        for (int i = 0; i < 10000; i++) {
            if (UpgradePool.pickRandom(duo, 1, u -> true, rng).get(0).getName().equals("rare")) {
                rare++;
            }
        }
        check(rare > 2000 && rare < 2600, "la carte rare sort environ 23 % du temps : " + rare);

        System.out.println("TestsPool : tout passe");
    }
}