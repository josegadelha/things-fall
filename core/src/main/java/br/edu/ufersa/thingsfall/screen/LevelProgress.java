package br.edu.ufersa.thingsfall.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

// Tracks which of the levels were completedLevels and persists that to a local JSON file.
public final class LevelProgress {
    public static final int LEVEL_COUNT = 5;
    private static final String FILE_NAME = "level-progress.json";

    // Serialized shape of the file must stay public with a no-arg constructor for Json.
    public static class Data {
        public boolean[] completedLevels = new boolean[LEVEL_COUNT];
        public long[][] completionTimes = new long[LEVEL_COUNT][];
    }

    private static Data data;

    private LevelProgress() {
    }

    public static boolean isUnlocked(int levelIndex) {
        if (levelIndex < 0 || levelIndex >= LEVEL_COUNT) {
            return false;
        }
        if (levelIndex == 0) {
            return true;
        }
        return load().completedLevels[levelIndex - 1];
    }

    public static boolean isCompleted(int levelIndex) {
        if (levelIndex < 0 || levelIndex >= LEVEL_COUNT) {
            return false;
        }
        return load().completedLevels[levelIndex];
    }

    public static long[] getCompletionTimes(int levelIndex) {
        if (levelIndex < 0 || levelIndex >= LEVEL_COUNT) {
            return new long[] {};
        }
        long[] ordered = load().completionTimes[levelIndex];
        if (ordered != null)
            quickSort(ordered, 0, ordered.length-1);
        return ordered;
    }

    private static void quickSort(long[] array, int start, int end)
    {
        int i = start, j = end;
        long pivot, aux;
        pivot = array[(start + end) / 2];

        while(i < j)
        {
            while(array[i] < pivot)
                i++;
            while(array[j] > pivot)
                j--;
            if (i <= j) {
                aux = array[i];
                array[i] = array[j];
                array[j] = aux;
                i++;
                j--;
            }
        }
        if (j > start) {
            quickSort(array, start, j);
        }
        if (i < end) {
            quickSort(array, i, end);
        }
    }

    // The sandbox opens once the last level is completedLevels.
    public static boolean isSandboxUnlocked() {
        return isCompleted(LEVEL_COUNT - 1);
    }

    // Call this when the player finishes a level.
    public static void markCompleted(int levelIndex, long completionTime) {
        if (levelIndex < 0 || levelIndex >= LEVEL_COUNT) {
            return;
        }

        Data current = load();
        if (!current.completedLevels[levelIndex]) {
            current.completedLevels[levelIndex] = true;
        }
        if (current.completionTimes[levelIndex] != null) {
            long[] temp = new long[current.completionTimes[levelIndex].length + 1];
            System.arraycopy(current.completionTimes[levelIndex], 0, temp, 0, current.completionTimes[levelIndex].length);
            temp[temp.length - 1] = completionTime;
            current.completionTimes[levelIndex] = temp;
        } else {
            current.completionTimes[levelIndex] = new long[] {
                completionTime
            };
        }
        save();
    }

    // Wipes all progress.
    public static void reset() {
        data = new Data();
        save();
    }

    private static Data load() {
        if (data != null) {
            return data;
        }

        data = new Data();
        FileHandle file = Gdx.files.local(FILE_NAME);
        if (file.exists()) {
            try {
                Data loaded = new Json().fromJson(Data.class, file);
                if (loaded != null) {
                    if (loaded.completedLevels != null) {
                        int count = Math.min(loaded.completedLevels.length, LEVEL_COUNT);
                        System.arraycopy(loaded.completedLevels, 0, data.completedLevels, 0, count);
                    }
                    if (loaded.completionTimes != null) {
                        for (int i = 0; i < LEVEL_COUNT; i++) {
                            if (loaded.completionTimes[i] != null) {
                                data.completionTimes[i] = new long[loaded.completionTimes[i].length];
                                System.arraycopy(loaded.completionTimes[i], 0, data.completionTimes[i], 0, loaded.completionTimes[i].length);
                            }
                        }
                    }
                }
            } catch (RuntimeException e) {
                Gdx.app.error("LevelProgress", "Could not read " + FILE_NAME + ", starting fresh", e);
                data = new Data();
            }
        }
        return data;
    }

    private static void save() {
        try {
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            Gdx.files.local(FILE_NAME).writeString(json.prettyPrint(data), false);
        } catch (RuntimeException e) {
            Gdx.app.error("LevelProgress", "Could not write " + FILE_NAME, e);
        }
    }
}
