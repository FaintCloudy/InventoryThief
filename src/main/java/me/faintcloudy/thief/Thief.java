package me.faintcloudy.thief;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.HashBiMap;
import net.minecraft.network.protocol.game.PacketPlayOutSetSlot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

public class Thief extends JavaPlugin implements Listener, CommandExecutor {
    String currentThief = "NONE";
    int steals = 0;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        /*new BukkitRunnable() {
            @Override
            public void run() {
                for (String player : inventoryViews.keySet()) {
                    if (Bukkit.getPlayer(player) == null)
                        continue;
                    boolean flag = false;
                    for (String updatePlayer : pauseUpdatePlayers) {
                        if (updatePlayer.equals(player)) {
                            flag = true;
                            break;
                        }
                    }

                    if (flag)
                    {
                        continue;
                    }

                    updateInventory(Bukkit.getPlayer(player));
                }
            }
        };//.runTaskTimer(this, 0, 1);*/

    }

    HashBiMap<String, Inventory> inventoryViews = HashBiMap.create();
    @EventHandler
    public void onInvSee(PlayerInteractAtEntityEvent event)
    {
        if (!event.getPlayer().getName().equals(currentThief))
            return;
        if (!(event.getRightClicked() instanceof Player target))
            return;
        Inventory inventory;
        if (inventoryViews.containsKey(target.getName())) {
            inventory = inventoryViews.get(target.getName());
        } else {
            inventory = this.createInventory(target);
            inventoryViews.put(target.getName(), inventory);
        }
        event.getPlayer().openInventory(inventory);
    }

    private Inventory createInventory(Player target)
    {
        Inventory inventory = Bukkit.createInventory(target, 45, target.getName());
        ItemStack barrier = new ItemStack(Material.BARRIER);
        inventory.setItem(41, barrier);
        inventory.setItem(42, barrier);
        inventory.setItem(43, barrier);
        inventory.setItem(44, barrier);
        return inventory;
    }

    /*private void updateInventory(Player target)
    {
        if (inventoryViews.get(target.getName()) == null)
            inventoryViews.put(target.getName(), this.createInventory(target));
        Inventory remoteInventory = inventoryViews.get(target.getName());
        PlayerInventory playerInventory = target.getInventory();
        for (int i = 0;i<playerInventory.getStorageContents().length;i++)
        {
            if (remoteInventory.getItem(i) != playerInventory.getItem(i))
                remoteInventory.setItem(i, playerInventory.getItem(i));
        }
        remoteInventory.setItem(36, playerInventory.getHelmet());
        remoteInventory.setItem(37, playerInventory.getChestplate());
        remoteInventory.setItem(38, playerInventory.getLeggings());
        remoteInventory.setItem(39, playerInventory.getBoots());
        remoteInventory.setItem(40, playerInventory.getItemInOffHand());
    }*/

    /*@EventHandler
    public void banDrag(InventoryDragEvent event)
    {
        if (inventoryViews.containsValue(event.getInventory()))
            event.setCancelled(true);
    }*/

    @EventHandler
    public void onHandleTargetInventoryClick(InventoryClickEvent event)
    {
        if (event.isCancelled())
            return;
        if (event.getClickedInventory() instanceof PlayerInventory && inventoryViews.containsKey(event.getWhoClicked().getName()) &&
                event.getClickedInventory() == event.getWhoClicked().getInventory()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    PlayerInventory localInventory = event.getWhoClicked().getInventory();
                    Inventory remoteInventory = inventoryViews.get(event.getWhoClicked().getName());
                    for (int i = 0;i<localInventory.getStorageContents().length;i++)
                    {
                        if (localInventory.getItem(i) != remoteInventory.getItem(i))
                            remoteInventory.setItem(i, localInventory.getItem(i));
                    }
                    remoteInventory.setItem(36, localInventory.getHelmet());
                    remoteInventory.setItem(37, localInventory.getChestplate());
                    remoteInventory.setItem(38, localInventory.getLeggings());
                    remoteInventory.setItem(39, localInventory.getBoots());
                    remoteInventory.setItem(40, localInventory.getItemInOffHand());
                }
            }.runTaskLater(this, 1);
        }
    }

    @EventHandler
    public void onHandleClick(InventoryClickEvent event)
    {
        if (event.isCancelled())
            return;
        if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER)
        {
            event.setCancelled(true);
            return;
        }
        if (event.getCursor() != null && event.getCursor().getType() == Material.BARRIER)
        {
            event.setCancelled(true);
            return;
        }
        if (event.getWhoClicked() instanceof Player player && player.getName().equals(currentThief))
        {
            if (!inventoryViews.containsValue(event.getWhoClicked().getOpenInventory().getTopInventory()))
            {
                return;
            }

            Player target = Bukkit.getPlayer(inventoryViews.inverse().get(event.getWhoClicked().getOpenInventory().getTopInventory()));
            //pauseUpdatePlayers.add(target.getName());
            if (event.getSlot() > 40)
            {
                event.setCancelled(true);
                return;
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    Player nTarget = Bukkit.getPlayer(target.getName());
                    PlayerInventory targetInventory = nTarget.getInventory();
                    Inventory remoteInventory = event.getInventory();
                    for (int i = 0;i<targetInventory.getStorageContents().length;i++)
                    {
                        if (targetInventory.getItem(i) != remoteInventory.getItem(i))
                            targetInventory.setItem(i, remoteInventory.getItem(i));
                    }
                    targetInventory.setHelmet(remoteInventory.getItem(36));
                    targetInventory.setChestplate(remoteInventory.getItem(37));
                    targetInventory.setLeggings(remoteInventory.getItem(38));
                    targetInventory.setBoots(remoteInventory.getItem(39));
                    targetInventory.setItemInOffHand(remoteInventory.getItem(40));


                    target.updateInventory();

                    //pauseUpdatePlayers.remove(target.getName());

                }
            }.runTaskLater(this, 1);

        }
    }

    @EventHandler
    public void onTakeItem(InventoryClickEvent event)
    {
        if (event.getWhoClicked().getName().equals(currentThief) && event.getClickedInventory() != null
                && event.getClickedInventory() != event.getWhoClicked().getInventory() && inventoryViews.containsValue(event.getClickedInventory()))
        {
            if (event.getCurrentItem() != null)
                steals += event.getCurrentItem().getAmount();
            check();
        }
    }

    public void resetThief(String name)
    {
        currentThief = name;
        steals = 0;
        Bukkit.getPlayer(currentThief).sendMessage("§c§l你成为了一名小偷, 请隐藏好自己！");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        if (currentThief.equals("NONE"))
        {
            resetThief(event.getPlayer().getName());

        }
    }

    public void check()
    {
        Player player = Bukkit.getPlayer(currentThief);
        player.setMaxHealth(20 + ((int) (steals * 2d / 10d)));
    }

    private boolean isThief(Player player)
    {
        return player.getName().equals(currentThief);
    }

    private void punish(Player player)
    {
        player.setMaxHealth(player.getMaxHealth()-6);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event)
    {
        if (!isThief(event.getEntity()))
        {
            if (event.getEntity().getKiller() != null && !isThief(event.getEntity().getKiller()))
            {
                punish(event.getEntity().getKiller());
            }
            event.getEntity().setMaxHealth(20);
        } else {
            event.getEntity().setMaxHealth(20);
            punish(event.getEntity());
            Random random = new Random();
            List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
            players.remove(event.getEntity());
            if (players.size() == 1)
                resetThief(players.get(0).getName());
            else
                resetThief(players.get(random.nextInt(players.size()-1)).getName());
            check();

        }
    }
}
