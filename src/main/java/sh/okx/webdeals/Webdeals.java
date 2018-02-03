package sh.okx.webdeals;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import sh.okx.webdeals.api.WebdealManager;
import sh.okx.webdeals.buycraft.BuycraftWebdealManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Webdeals extends JavaPlugin {
    private Gson gson = new Gson();
    private WebdealManager manager;
    private FileConfiguration messages;
    private WebdealsCommand command;

    private Economy econ = null;

    public WebdealManager getManager() {
        return manager;
    }

    public Economy getEcon() {
        return econ;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public void onEnable() {
        if(!setupEconomy()) {
            getLogger().warning("Vault or an economy plugin has not been found. Buying has been disabled.");
        }
        messages = getConfig("messages");

        saveDefaultConfig();
        setupManager();

        getCommand("webdeals").setExecutor(command = new WebdealsCommand(this));
    }

    public boolean canBuy() {
        return getBuyCost() > 0 && econ != null;
    }

    public int getBuyCost() {
        return getConfig().getInt("buy");
    }

    public void reload() {
        loadConfig(messages, "messages");
        loadConfig(manager.getConfig(), "balances");

        command.reload();
        manager.reload();

        reloadConfig();
    }

    private void setupManager() {
        PluginManager plugins = Bukkit.getServer().getPluginManager();

        if(plugins.isPluginEnabled("BuycraftX")) {
            getLogger().info("Using BuycraftX as the webdeal manager.");
            this.manager = new BuycraftWebdealManager(this);
        } else {
            getLogger().severe("No suitable webdeal plugin found, disabling. Applicable plugins are: " +
                    String.join(", ", getDescription().getSoftDepend()));
            plugins.disablePlugin(this);
        }
    }

    public FileConfiguration getConfig(String name) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(createFile(name));

        InputStream stream = getResource(name + ".yml");
        if (stream != null) {
            config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(stream, Charsets.UTF_8)));
        }

        config.options().copyDefaults(true);
        saveConfig(config, name);

        return config;
    }

    public void saveConfig(FileConfiguration config, String name) {
        try {
            config.save(createFile(name));
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("There was an error saving the " + name + "file.");
        }
    }

    public void loadConfig(FileConfiguration config, String name) {
        try {
            config.load(createFile(name));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            getLogger().severe("There was an error loading the " + name + "file.");
        }
    }

    private File createFile(String name) {
        try {
            File data = getDataFolder();
            data.mkdir();
            File file = new File(data + File.separator + name + ".yml");
            file.createNewFile();
            return file;
        } catch(IOException ex) {
            ex.printStackTrace();
            getLogger().severe("There was an error creating the " + name + " file.");
            return null;
        }
    }

    public Gson getGson() {
        return gson;
    }

    public void sendMessage(CommandSender cs, String message, Object... args) {
        String result = getMessage(message, args);
        if(!result.isEmpty()) {
            cs.sendMessage(result);
        }
    }

    public String getMessage(String message, Object... args) {
        return ChatColor.translateAlternateColorCodes('&',
                String.format(messages.getString(message), args));
    }
}
