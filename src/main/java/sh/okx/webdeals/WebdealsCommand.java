
package sh.okx.webdeals;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import sh.okx.webdeals.gui.Gui;
import sh.okx.webdeals.handlers.BalanceHandler;
import sh.okx.webdeals.handlers.HelpHandler;
import sh.okx.webdeals.handlers.ListCouponsHandler;
import sh.okx.webdeals.handlers.RedeemHandler;
import sh.okx.webdeals.util.ItemBuilder;
import sh.okx.webdeals.util.NumberUtil;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.BiConsumer;

public class WebdealsCommand implements CommandExecutor {
    private DecimalFormat basicDf = new DecimalFormat("#,##0.##");
    private Webdeals plugin;

    private Gui root;
    private Gui redeem;

    public WebdealsCommand(Webdeals plugin) {
        this.plugin = plugin;

        reload();
    }

    public void reload() {
        if(root != null) {
            root.unregister();
        }
        if(redeem != null) {
            redeem.unregister();
        }

        List<Double> amounts = plugin.getConfig().getDoubleList("coupons.amounts");
        redeem = new Gui(plugin, "Redeem", NumberUtil.roundUp(amounts.size(), 9) / 9);

        for(int i = 0; i < amounts.size(); i++) {
            double amount = amounts.get(i);

            redeem.register(new ItemBuilder(Material.PAPER)
                    .setDisplayName(plugin.getMessage("gui.redeem.name", plugin.getManager().format(amount)))
                    .build(), i, new RedeemHandler(plugin, amount));
        }

        root = new Gui(plugin, "Webdeals", 1);
        root.register(new ItemBuilder(Material.PAPER)
                .setDisplayName(plugin.getMessage("gui.root.balance.name"))
                .setLore(plugin.getMessage("gui.root.balance.description").split("\n"))
                .build(), 0, new BalanceHandler(plugin));
        root.register(new ItemBuilder(Material.PAINTING)
                .setDisplayName(plugin.getMessage("gui.root.redeem.name"))
                .setLore(plugin.getMessage("gui.root.redeem.description").split("\n"))
                .build(), 1, e -> redeem.open(e.getWhoClicked()));
        root.register(new ItemBuilder(Material.ARROW)
                .setDisplayName(plugin.getMessage("gui.root.coupons.name"))
                .setLore(plugin.getMessage("gui.root.coupons.description").split("\n"))
                .build(), 2, new ListCouponsHandler(plugin));
        root.register(new ItemBuilder(Material.SIGN)
                .setDisplayName(plugin.getMessage("gui.root.help.name"))
                .setLore(plugin.getMessage("gui.root.help.description").split("\n"))
                .build(), 3, new HelpHandler());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            plugin.sendMessage(sender, "error.player_required");
            return true;
        }

        Player player = (Player) sender;
        if(args.length > 0) {
            switch(args[0].toLowerCase()) {
                case "add":
                    changeBalance(player, "add", label, args, (p, d) -> plugin.getManager().add(p, d));
                    return true;
                case "take":
                    changeBalance(player, "take", label, args, (p, d) -> plugin.getManager().take(p, d));
                    return true;
                case "set":
                    changeBalance(player, "set", label, args, (p, d) -> plugin.getManager().set(p, d));
                    return true;
                case "pay":
                case "give":
                    if(!player.hasPermission("webdeals.command.pay")) {
                        plugin.sendMessage(player, "error.no_permission");
                        return true;
                    }

                    if(args.length != 3) {
                        plugin.sendMessage(player, "command.pay.usage", label);
                        return true;
                    }

                    Player pay = Bukkit.getPlayer(args[1]);
                    if (pay == null) {
                        plugin.sendMessage(player, "error.invalid_player", args[1]);
                        return true;
                    }

                    double amount;
                    try {
                        amount = Double.parseDouble(args[2]);
                    } catch(NumberFormatException e) {
                        plugin.sendMessage(player, "error.invalid_number", args[2]);
                        return true;
                    }

                    if(amount < 0) {
                        plugin.sendMessage(player, "error.invalid_number", args[2]);
                        return true;
                    }

                    if(plugin.getManager().getBalance(player) < amount) {
                        plugin.sendMessage(player, "command.pay.not_enough");
                        return true;
                    }

                    plugin.getManager().take(player, amount);
                    plugin.getManager().add(pay, amount);

                    plugin.sendMessage(player, "command.pay.success", plugin.getManager().format(amount), pay.getName(),
                            plugin.getManager().format(plugin.getManager().getBalance(player)));
                    return true;
                case "reload":
                    if(!player.hasPermission("webdeals.command.admin.reload")) {
                        plugin.sendMessage(player, "error.no_permission");
                        return true;
                    }

                    plugin.reload();
                    plugin.sendMessage(player, "command.reload.success");
                    return true;
                case "balance":
                case "bal":
                    if(!player.hasPermission("webdeals.command.balance")) {
                        plugin.sendMessage(player, "error.no_permission");
                        return true;
                    }

                    if(args.length > 2) {
                        plugin.sendMessage(player, "command.balance.usage", label);
                        return true;
                    }

                    Player bal = args.length == 2 ? Bukkit.getPlayer(args[1]) : player;
                    if (bal == null) {
                        plugin.sendMessage(player, "error.invalid_player", args[1]);
                        return true;
                    }

                    if(args.length == 2 && player.hasPermission("webdeals.command.balance.other")) {
                        plugin.sendMessage(player, "command.balance.success.other",
                                bal.getName(), plugin.getManager().format(plugin.getManager().getBalance(bal)));
                    } else {
                        plugin.sendMessage(player, "command.balance.success.self",
                                bal.getName(), plugin.getManager().format(plugin.getManager().getBalance(bal)));
                    }
                    return true;
                case "buy":
                case "purchase":
                    if(!player.hasPermission("webdeals.command.buy")) {
                        plugin.sendMessage(player, "error.no_permission");
                        return true;
                    }

                    if(!plugin.canBuy()) {
                        plugin.sendMessage(player, "command.buy.disabled");
                        return true;
                    }

                    if(args.length != 2) {
                        plugin.sendMessage(player, "command.buy.usage", label);
                        return true;
                    }

                    double buy;
                    try {
                        buy = Double.parseDouble(args[1]);
                    } catch(NumberFormatException e) {
                        plugin.sendMessage(player, "error.invalid_number", args[1]);
                        return true;
                    }

                    if(buy < 0) {
                        plugin.sendMessage(player, "error.invalid_number", args[1]);
                        return true;
                    }

                    double needed = plugin.getBuyCost() * buy;
                    double balance = plugin.getEcon().getBalance(player);

                    if(needed > balance) {
                        plugin.sendMessage(player, "command.buy.not_enough", basicDf.format(needed));
                        return true;
                    }

                    plugin.getEcon().withdrawPlayer(player, needed);
                    plugin.getManager().add(player, buy);

                    plugin.sendMessage(player, "command.buy.success",
                            plugin.getManager().format(buy),
                            basicDf.format(needed),
                            plugin.getManager().format(plugin.getManager().getBalance(player)));

                    return true;
                case "help":
                    if(!player.hasPermission("webdeals.command.help")) {
                        plugin.sendMessage(player, "error.no_permission");
                        return true;
                    }

                    PluginDescriptionFile desc = plugin.getDescription();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&a&l" + desc.getName() + " " + desc.getVersion() +
                            " &eby &9&l" + String.join(", ", desc.getAuthors())));

                    sendHelp(player, "add", label);
                    sendHelp(player, "take", label);
                    sendHelp(player, "set", label);
                    sendHelp(player, "reload", label);
                    sendHelp(player, "balance", label);
                    sendHelp(player, "pay", label);
                    if(plugin.canBuy()) {
                        sendHelp(player, "buy", label);
                    }

                    return true;
            }
        }

        root.open(player);

        return true;
    }

    private void sendHelp(Player player, String type, String label) {
        if(player.hasPermission(plugin.getMessage("command.help." + type + ".permission"))) {
            String commandUsage = plugin.getMessage("command." + type + ".usage", label);
            plugin.sendMessage(player, "command.help." + type + ".usage", commandUsage);
        }
    }

    private void changeBalance(Player player, String type, String label, String[] args, BiConsumer<Player, Double> then) {
        if(args.length < 2 || args.length > 3) {
            plugin.sendMessage(player, "command." + type + ".usage", label);
            return;
        }

        if(!player.hasPermission("webdeals.command.admin." + type)) {
            plugin.sendMessage(player, "error.no_permission");
            return;
        }

        Player set = args.length == 3 ? Bukkit.getPlayer(args[1]) : player;
        if (set == null) {
            plugin.sendMessage(player, "error.invalid_player", args[1]);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[args.length == 3 ? 2 : 1]);
        } catch(NumberFormatException e) {
            plugin.sendMessage(player, "error.invalid_number", args[args.length == 3 ? 2 : 1]);
            return;
        }

        if(amount < 0) {
            plugin.sendMessage(player, "error.invalid_number", args[2]);
            return;
        }

        then.accept(set, amount);
        plugin.sendMessage(player, "command." + type + ".success",
                plugin.getManager().format(amount), set.getName(),
                plugin.getManager().format(plugin.getManager().getBalance(set)));
    }
}
