package me.oncode9.lpc;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Handles Spigot server software.
 */
@SuppressWarnings("deprecation")
public class SpigotServer implements Listener {

  @EventHandler(priority = EventPriority.LOWEST)
  public void onChat(AsyncPlayerChatEvent event) {
    LPCPlugin.core().getLogger().info("spigot chat event");
    event.setFormat(
        LPCPlugin.LEGACY_COMPONENT_SERIALIZER.serialize(LPCPlugin.core().formatChat(event.getPlayer(), event.getMessage()))
    );
  }
}
