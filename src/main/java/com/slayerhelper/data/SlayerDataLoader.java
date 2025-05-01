package com.slayerhelper.data;

import com.slayerhelper.domain.SlayerTask;
import lombok.extern.slf4j.Slf4j;
import net.runelite.http.api.RuneLiteAPI;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Slf4j
public class SlayerDataLoader {

    private String jsonFilePath = "/data/slayerTasks.json";
    private static final LocationDataLoader locationDataLoader = new LocationDataLoader();

    public Collection<SlayerTask> load() {
        try (InputStream inputStream = this.getClass().getResourceAsStream(jsonFilePath)) {
            if (inputStream == null) {
                log.error("JSON file not found: {}", jsonFilePath);
                return Collections.emptyList();
            }
            try (Reader reader = new InputStreamReader(inputStream)) {
                SlayerTask[] tasks = RuneLiteAPI.GSON.fromJson(reader, SlayerTask[].class);
                return Arrays.asList(tasks);
            }
        } catch (IOException e) {
            log.error("Could not read JSON from slayerTasks.json", e);
            return Collections.emptyList();
        }
    }


    public void setJsonFilePathForTesting(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
    }
}
