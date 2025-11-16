package com.scala.plot;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.bukkit.Bukkit.*;


public class VersionChecker {
    public class needUpdate {
        public boolean needUpdate;

    }


    public static void checkVersion() {
        try {

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://pastebin.com/raw/1PH2TvLG"))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (!response.body().equals("1.1.4")) {
                getLogger().warning("[Plot] Update available! Install it from: https://modrinth.com/plugin/plot");
            } else {
                getLogger().info("[Plot] No update avaiable!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
