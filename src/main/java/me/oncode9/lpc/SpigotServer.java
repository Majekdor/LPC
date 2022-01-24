package me.oncode9.lpc;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Handles Spigot server software.
 */
@SuppressWarnings("deprecation")
public final class SpigotServer implements Listener {

  @EventHandler
  public void onChat(AsyncPlayerChatEvent event) {
    event.setFormat(
        LPCPlugin.LEGACY_COMPONENT_SERIALIZER.serialize(LPCPlugin.core().formatChat(event.getPlayer(), event.getMessage()))
    );
  }
}
