package hugo.wave;

public class WaveManager {
    private int wave = 1 ;
    private int cycle = 1 ;

    private static float WAVE_DURATION = 60f;
    private float elapsedTime = 0f;

    public boolean update(float delta) {
    elapsedTime += delta;

    boolean waveChanged = false;

    while (elapsedTime >= WAVE_DURATION) {
        elapsedTime -= WAVE_DURATION;
        NextWave();
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

public float getDamageMultiplier() {
    return (float) Math.pow(1.2, cycle - 1);  // = « 2 à la puissance cycle - 1 ». On retire 1 parce que le premier cycle commence à ×1.
}


    public void NextWave() {
        wave++;
        if (wave> 5) {
            wave = 1;
            cycle++;
        }
    }
}
