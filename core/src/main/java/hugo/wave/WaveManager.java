package hugo.wave;

public class WaveManager {
    private static final float WAVE_DURATION = 25f;
    private static final int WAVES_PER_CYCLE = 5;

    private int wave = 1;
    private int cycle = 1;
    private float elapsedTime;

    // chrono vague
    public boolean update(float delta) {
        elapsedTime += delta;
        boolean waveChanged = false;
        while (elapsedTime >= WAVE_DURATION) {
            elapsedTime -= WAVE_DURATION;
            nextWave();
            waveChanged = true;
        }
        return waveChanged;
    }

    public int getWave() {
        return wave;
    }

    public int getCycle() {
        return cycle;
    }

    // mobs plus forts à chaque cycle
    public float getDamageMultiplier() {
        return (float) Math.pow(1.2, cycle - 1);
    }

    // vague suivante
    private void nextWave() {
        wave++;
        if (wave > WAVES_PER_CYCLE) {
            wave = 1;
            cycle++;
        }
    }
}
