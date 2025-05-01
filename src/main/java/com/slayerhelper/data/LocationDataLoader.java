package com.slayerhelper.data;

import com.slayerhelper.domain.Location;
import lombok.extern.slf4j.Slf4j;
import net.runelite.http.api.RuneLiteAPI;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LocationDataLoader {
    private Map<String, Location> locationMap = new HashMap<>();

    public void load() {
        String jsonFilePath = "/data/locations.json";
        try (InputStream inputStream = this.getClass().getResourceAsStream(jsonFilePath)) {
            if (inputStream == null) {
                log.error("JSON file not found: {}", jsonFilePath);
                return;
            }
            try (Reader reader = new InputStreamReader(inputStream)) {
                Location[] locations = RuneLiteAPI.GSON.fromJson(reader, Location[].class);
                Map<String, Location> result = new HashMap<>();
                for (Location location : locations) {
                    result.put(location.getName().toLowerCase(), location);
                }
                this.locationMap = result;
            }
        } catch (IOException e) {
            log.error("Could not read JSON from locations.json", e);
        }
    }

    public Location getLocation(String name) {
        if (locationMap.isEmpty()) {
            load();
        }
        return locationMap.get(name.toLowerCase());
    }
}
