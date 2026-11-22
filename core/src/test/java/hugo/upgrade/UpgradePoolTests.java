package hugo.upgrade;

import hugo.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public final class UpgradePoolTests {

    private static void check(boolean ok, String text) {
        if (!ok) throw new AssertionError(text);
    }

    // dummy upgrade for the tests
    private static Upgrade card(String name, float weight) {
        return new Upgrade(name, name, weight) {
            @Override
            public void apply(Player player) {}
        };
    }

    public static void main(String[] args) {
        Random rng = new Random(42); // fixed seed for repeatable tests

        List<Upgrade> cards = new ArrayList<>();
        cards.add(card("a", 1));
        cards.add(card("b", 1));
        cards.add(card("c", 1));
        cards.add(card("d", 1));
        cards.add(card("rare", 0.3f));
        cards.add(card("never", 0));

        for (int i = 0; i < 1000; i++) {
            List<Upgrade> picked = UpgradePool.pickRandom(cards, 3, u -> true, rng);
            check(picked.size() == 3, "three cards drawn");
            check(new HashSet<>(picked).size() == 3, "no duplicate cards");
            for (Upgrade u : picked) {
                check(!u.getName().equals("never"), "zero-weight cards never appear");
            }
        }

        for (int i = 0; i < 1000; i++) {
            List<Upgrade> picked =
                    UpgradePool.pickRandom(cards, 3, u -> !u.getName().equals("a"), rng);
            for (Upgrade u : picked) {
                check(!u.getName().equals("a"), "unavailable cards never appear");
            }
        }

        List<Upgrade> few =
                UpgradePool.pickRandom(
                        cards, 3, u -> u.getName().equals("a") || u.getName().equals("b"), rng);
        check(few.size() == 2, "return both available cards");

        List<Upgrade> duo = new ArrayList<>();
        duo.add(card("speed", 1));
        duo.add(card("rare", 0.3f));
        int rare = 0;
        for (int i = 0; i < 10000; i++) {
            if (UpgradePool.pickRandom(duo, 1, u -> true, rng).get(0).getName().equals("rare")) {
                rare++;
            }
        }
        check(
                rare > 2000 && rare < 2600,
                "rare card appears about 23 percent of the time: " + rare);

        System.out.println("UpgradePoolTests: all checks passed");
    }
}
