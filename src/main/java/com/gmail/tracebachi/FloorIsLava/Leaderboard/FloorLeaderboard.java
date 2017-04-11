package com.gmail.tracebachi.FloorIsLava.Leaderboard;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.tracebachi.FloorIsLava.FloorIsLavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Jeremy Lugo on 4/10/2017.
 */
public class FloorLeaderboard
{
    private Map<String, LeaderboardEntry> entries;
    private List<Hologram> holograms;
    private File file;
    private FileConfiguration config;

    private int maxEntries = 10;

    public FloorLeaderboard(File file)
    {
        entries = new HashMap<>();
        holograms = new ArrayList<>();
        config = YamlConfiguration.loadConfiguration(file);
        this.file = file;

        if(!file.exists())
        {
            try
            {
                file.createNewFile();
                config.set("LeaderboardTitle", "&8[&6FloorIsLava Leaderboard&8]");
                config.set("MaxEntries", 10);
                config.set("Locations", new ArrayList<>());
                config.save(file);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        maxEntries = config.getInt("MaxEntries");
    }

    public void addOneToScore(String name)
    {
        LeaderboardEntry entry = entries.getOrDefault(name, new LeaderboardEntry(name, 0));
        entry.setScore(entry.getScore() + 1);
        entries.put(name, entry);
    }

    public void addNewLeaderboard(Location location)
    {
        Hologram hologram = spawnHolo(location);
        setupHolo(hologram);
        holograms.add(hologram);

        save();
    }

    public boolean removeLeaderboard(int index)
    {
        Hologram hologram = holograms.remove(index);
        if(hologram != null)
        {
            hologram.delete();
            save();
            return true;
        }
        return false;
    }

    public void recalculate()
    {
        for(Hologram hologram : holograms)
        {
            clearHolo(hologram);
            setupHolo(hologram);
        }
    }

    public void save()
    {
        saveEntries();
        saveLocations();
        try
        {
            config.save(file);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void load()
    {
        loadEntries();
        loadLocations();
    }

    public void clear()
    {
        for(Hologram hologram : holograms)
        {
            clearHolo(hologram);
        }
    }

    private void loadEntries()
    {
        ConfigurationSection section = config.getConfigurationSection("Entries");
        if(section == null)
        {
            return;
        }
        for(String entry : section.getKeys(false))
        {
            String name = section.getString(entry + ".Name");
            int score = section.getInt(entry + ".Score");
            entries.put(name, new LeaderboardEntry(name, score));
        }
    }

    private void loadLocations()
    {
        List<Location> locations = new ArrayList<>();
        locations = (List<Location>) config.getList("Locations");
        for(Location location : locations)
        {
            addNewLeaderboard(location);
        }
    }

    private void saveEntries()
    {
        int c = 0;
        for(LeaderboardEntry entry : entries.values())
        {
            config.set("Entries." + c + ".Name", entry.getName());
            config.set("Entries." + c + ".Score", entry.getScore());
            c++;
        }
    }

    private void saveLocations()
    {
        List<Location> locations = new ArrayList<>();
        for(Hologram hologram : holograms)
        {
            locations.add(hologram.getLocation());
        }
        config.set("Locations", locations);
    }

    private Hologram spawnHolo(Location location)
    {
        Hologram hologram = HologramsAPI.createHologram(FloorIsLavaPlugin.getInstance(), location);
        return hologram;
    }

    private void setupHolo(Hologram hologram)
    {
        hologram.appendTextLine(translate(config.getString("LeaderboardTitle")));
        List<LeaderboardEntry> leaderboardEntries = new ArrayList<>(entries.values());
        leaderboardEntries.sort(Comparator.reverseOrder());
        for(int i = 0; i < leaderboardEntries.size() && i < maxEntries; i++)
        {
            LeaderboardEntry entry = leaderboardEntries.get(i);
            hologram.appendTextLine(translate(
                "&6#" + (i + 1) + ". " + entry.getName() + " &8- &a" + entry.getScore()));
        }
    }

    private void clearHolo(Hologram hologram)
    {
        hologram.clearLines();
    }

    private String translate(String input)
    {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
