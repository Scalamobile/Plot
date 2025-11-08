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
                    .uri(URI.create("https://gist.githubusercontent.com/Scalamobile/f84a2b9546edecb92e9b6516390ad6f4/raw/1c77e69ad5b70b28ef71f085497e0edc5b515d0d/gistfile1.txt"))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (!response.body().equals("1.1.2")) {
                getLogger().warning("[Plot] Update available! Install it from: https://modrinth.com/plugin/plot");
            } else {
                getLogger().info("[Plot] No update avaiable!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
