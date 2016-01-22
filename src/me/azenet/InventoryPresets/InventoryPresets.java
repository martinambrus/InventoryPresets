/*
 * Decompiled with CFR 0_110.
 *
 * Could not load the following classes:
 *  com.nijiko.permissions.PermissionHandler
 *  com.nijikokun.bukkit.Permissions.Permissions
 *  org.bukkit.Bukkit
 *  org.bukkit.ChatColor
 *  org.bukkit.Server
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.PlayerInventory
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.PluginDescriptionFile
 *  org.bukkit.plugin.PluginManager
 *  org.bukkit.plugin.java.JavaPlugin
 *  ru.tehkode.permissions.PermissionManager
 *  ru.tehkode.permissions.bukkit.PermissionsEx
 */
package me.azenet.InventoryPresets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryPresets extends JavaPlugin implements Listener {
	private static double version = 0.3;
    private static String mainDirectory = "plugins/InventoryPresets";
    private static File SettingsFile = new File(String.valueOf(mainDirectory) + File.separator + "settings.txt");
    private static File DatabaseFile = new File(String.valueOf(mainDirectory) + File.separator + "inv.db");
    private static Properties prop = new Properties();
    private Map<Player, String> currentInventory = new HashMap<Player, String>();
    private static HashSet<Object> inventories = new HashSet<Object>();
    public SLAPI slapi = new SLAPI();
    private Logger log = Logger.getLogger("Minecraft");
 	private net.milkbowl.vault.permission.Permission perms = null;

    @SuppressWarnings("unchecked")
	@Override
	public void onEnable() {
        new File(mainDirectory).mkdir();
        if (!SettingsFile.exists()) {
            prop = new Properties();
            try {
                SettingsFile.createNewFile();
                FileOutputStream out = new FileOutputStream(SettingsFile);
                prop.put("UseOPs", "no");
                prop.store(out, "Default configuration file for InventoryPresets. If you set UseOPs to yes, it will use OPs and only OPs. Permissions support will be disabled.");
                out.flush();
                out.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
        }
        if (!DatabaseFile.exists()) {
            try {
                DatabaseFile.createNewFile();
                SLAPI.save(InventoryPresets.inventories, String.valueOf(mainDirectory) + File.separator + "inv.db");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.initPerms();
        try {
            InventoryPresets.inventories = (HashSet<Object>)SLAPI.load(String.valueOf(mainDirectory) + File.separator + "inv.db");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        this.getServer().getPluginManager().registerEvents(this, this);

        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				try {
		            SLAPI.save(InventoryPresets.inventories, String.valueOf(mainDirectory) + File.separator + "inv.db");
		        }
		        catch (Exception e) {
		            e.printStackTrace();
		        }
			}
		}, 20 * 300, 20 * 300);

        this.log.info("[InventoryPresets] InventoryPresets v" + version + " is now enabled.");
    }

    public static HashSet<Object> getInventories() {
    	return inventories;
    }

    private boolean initPerms() {
    	if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
	        RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> rsp = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
	        this.perms = rsp.getProvider();
	        return this.perms != null;
		} else {
			return false;
		}
    }

    private boolean perms(Player plyr, String node) {
    	if (this.perms != null) {
			return this.perms.has(plyr, node);
		} else {
			return plyr.hasPermission(node);
		}
    }

    @SuppressWarnings("deprecation")
	private void writeInventory(Player plyr, String name, Boolean showConfirm) {
        Object damage;
        ItemStack[] inventory = plyr.getInventory().getContents();
        String finalchain = "";
        ItemStack[] arritemStack = inventory;
        int n = arritemStack.length;
        int n2 = 0;
        while (n2 < n) {
            ItemStack item = arritemStack[n2];
            if (item != null) {
                Integer id = 1;
                damage = 0;
                Integer stackSize = 0;
                id = item.getTypeId();
                stackSize = item.getAmount();
                damage = item.getDurability();
                finalchain = String.valueOf(finalchain) + id.toString() + ":" + stackSize.toString() + ":" + damage.toString() + ":" + n2 + ";";
            } else {
                finalchain = String.valueOf(finalchain) + "0:0:0:0;";
            }
            ++n2;
        }
        Object[] arrayInv = InventoryPresets.inventories.toArray();
        Integer erreur = 0;
        damage = arrayInv;
        int id = arrayInv.length;
        int n3 = 0;
        while (n3 < id) {
            Object toCastInv = arrayInv[n3];
            HashMap<?, ?> thisInv = (HashMap<?, ?>)toCastInv;
            if (thisInv.containsKey("Preset" + plyr.getName() + name)) {
                erreur = 1;
            }
            ++n3;
        }
        if (erreur == 1) {
        	if (showConfirm) {
        		plyr.sendMessage(ChatColor.RED + "This one is already existent. Please remove it first with /ipremove.");
        	} else {
        		log.warning("Failed to save player inventory when closing it - player: " + plyr.getName() + ", error: Inventory name already exists");
        	}
            return;
        }
        HashMap<String, String> invToWrite = new HashMap<String, String>();
        invToWrite.put("Preset" + plyr.getName() + name, finalchain);
        InventoryPresets.inventories.add(invToWrite);
        this.currentInventory.put(plyr, name);

        if (showConfirm) {
        	plyr.sendMessage(ChatColor.DARK_AQUA + "Your preset was saved successfully as " + name + ". Use /iprecall " + name + " to recall it.");
        }
    }

    private void recallInventory(Player plyr, String invname, Boolean showConfirm) {
        Object[] arrayInv = InventoryPresets.inventories.toArray();
        Boolean found = false;
        Object[] arrobject = arrayInv;
        int n = arrobject.length;
        int n2 = 0;
        while (n2 < n) {
            Object toCastInv = arrobject[n2];
            HashMap<?, ?> thisInv = (HashMap<?, ?>)toCastInv;
            if (thisInv.containsKey("Preset" + plyr.getName() + invname)) {
                found = true;
                String notParsedInventory = (String)thisInv.get("Preset" + plyr.getName() + invname);
                if (notParsedInventory == null) {
                	if (showConfirm) {
                		plyr.sendMessage(ChatColor.RED + "This preset does not exists.");
                	} else {
                		log.warning("Failed to load player inventory when closing it - player: " + plyr.getName() + ", error: Inventory does not exist (" + invname + ")");
                	}
                    return;
                }
                plyr.getInventory().clear();
                String[] arrstring = notParsedInventory.split(";");
                int n3 = arrstring.length;
                int n4 = 0;
                while (n4 < n3) {
                    String invslot = arrstring[n4];
                    String[] thisItem = invslot.split(":");
                    Integer id = Integer.parseInt(thisItem[0]);
                    Integer stackSize = Integer.parseInt(thisItem[1]);
                    Short damage = Short.parseShort(thisItem[2]);
                    if (id != 0) {
                        @SuppressWarnings("deprecation")
						ItemStack thisStack = new ItemStack(id.intValue(), stackSize.intValue(), damage.shortValue());
                        plyr.getInventory().setItem(Integer.parseInt(thisItem[3]), thisStack);
                    }
                    ++n4;
                }
            }
            ++n2;
        }
        if (!found.booleanValue()) {
        	if (showConfirm) {
        		plyr.sendMessage(ChatColor.RED + "This preset does not exists.");
        	} else {
        		log.warning("Failed to load player inventory when closing it - player: " + plyr.getName() + ", error: Inventory does not exist (" + invname + ") (2)");
        	}
            return;
        }
        this.currentInventory.put(plyr, invname);

        if (showConfirm) {
        	plyr.sendMessage(ChatColor.DARK_AQUA + "Preset " + invname + " was successfully recalled.");
        }
    }

    private void listPresets(Player plyr) {
        Object[] arrayInv = InventoryPresets.inventories.toArray();
        Boolean foundOne = false;
        Object[] arrobject = arrayInv;
        List<String> names = new ArrayList<String>();
        int n = arrobject.length;
        int n2 = 0;
        while (n2 < n) {
            Object toCastInv = arrobject[n2];
            HashMap<?, ?> thisInv = (HashMap<?, ?>)toCastInv;
            Set<?> thisKeyList = thisInv.keySet();
            Object[] stringKeyList = thisKeyList.toArray();
            if (stringKeyList[0].toString().indexOf(plyr.getName()) != -1) {
                foundOne = true;
                String[] currentName = stringKeyList[0].toString().split("Preset" + plyr.getName());
                names.add(currentName[1]);
               	plyr.sendMessage(ChatColor.DARK_AQUA + "- " + currentName[1]);
            }
            ++n2;
        }
        if (!foundOne.booleanValue()) {
            plyr.sendMessage(ChatColor.DARK_AQUA + "You don't have any presets. Use /ipsave to create one.");
        }
    }

    private void removePreset(Player plyr, String name, Boolean showConfirm) {
        Object[] arrayInv = InventoryPresets.inventories.toArray();
        Boolean found = false;
        Object[] arrobject = arrayInv;
        int n = arrobject.length;
        int n2 = 0;
        while (n2 < n) {
            Object toCastInv = arrobject[n2];
            HashMap<?, ?> thisInv = (HashMap<?, ?>)toCastInv;
            if (thisInv.containsKey("Preset" + plyr.getName() + name)) {
                found = true;
                InventoryPresets.inventories.remove(thisInv);
            }
            ++n2;
        }
        if (!found.booleanValue()) {
        	if (showConfirm) {
        		plyr.sendMessage(ChatColor.RED + "This preset does not exists.");
        	} else {
        		log.warning("Failed to load player inventory when closing it - player: " + plyr.getName() + ", error: Inventory does not exist (" + name + ")");
        	}
            return;
        }

        if (showConfirm) {
        	plyr.sendMessage(ChatColor.DARK_AQUA + "Preset " + name + " was successfully removed.");
        }
    }

    @Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Player plyr = (Player)sender;
        if (cmd.getName().equalsIgnoreCase("ipsave")) {
            if (this.perms(plyr, "inventorypresets.save")) {
                if (args.length == 1) {
                    this.writeInventory(plyr, args[0], true);
                    return true;
                }
                return false;
            }
            plyr.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("iprecall")) {
            if (args.length == 1) {
                if (this.perms(plyr, "inventorypresets.recall")) {
                    if (args.length == 1) {
                        this.recallInventory(plyr, args[0], true);
                        return true;
                    }
                    return false;
                }
                plyr.sendMessage(ChatColor.RED + "You don't have permission.");
                return true;
            }
            return false;
        }
        if (cmd.getName().equalsIgnoreCase("ipclear")) {
            if (!this.perms(plyr, "inventorypresets.clear")) {
                plyr.sendMessage(ChatColor.RED + "You don't have permission.");
                return true;
            }
            plyr.getInventory().clear();
            plyr.sendMessage(ChatColor.DARK_AQUA + "Inventory cleared.");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("iplist")) {
            if (!this.perms(plyr, "inventorypresets.list")) {
                plyr.sendMessage(ChatColor.RED + "You don't have permission.");
                return true;
            }
            this.listPresets(plyr);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("ipremove")) {
            if (args.length == 1) {
                if (this.perms(plyr, "inventorypresets.remove")) {
                    this.removePreset(plyr, args[0], true);
                    return true;
                }
                plyr.sendMessage(ChatColor.RED + "You don't have permission.");
                return true;
            }
            return false;
        }
        return false;
    }

    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
    public void onInventoryClose(InventoryCloseEvent event) {
    	Player player = (Player) event.getPlayer();

    	if (currentInventory.containsKey(player)) {
    		String inventoryName = currentInventory.get(player);
    		removePreset(player, inventoryName, false);
    		writeInventory(player, inventoryName, false);
    	}
    }

    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
    public void onItemDrop(PlayerDropItemEvent event) {
    	Player player = event.getPlayer();

    	if (currentInventory.containsKey(player)) {
    		String inventoryName = currentInventory.get(player);
    		removePreset(player, inventoryName, false);
    		writeInventory(player, inventoryName, false);
    	}
    }

    @EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
    public void onItemPickup(PlayerPickupItemEvent event) {
    	Player player = event.getPlayer();

    	if (currentInventory.containsKey(player)) {
    		String inventoryName = currentInventory.get(player);
    		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
				@Override
				public void run() {
					removePreset(player, inventoryName, false);
		    		writeInventory(player, inventoryName, false);
				}
			}, 2);
    	}
    }

    @Override
	public void onDisable() {
        try {
            SLAPI.save(InventoryPresets.inventories, String.valueOf(mainDirectory) + File.separator + "inv.db");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.log.info("[InventoryPresets] InventoryPresets v" + version + " is now disabled.");
    }
}

