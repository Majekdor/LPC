package me.oncode9.lpc;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Handles Paper server software.
 */
public class PaperServer implements Listener {

  @EventHandler(priority = EventPriority.LOWEST)
  public void onChat(final AsyncChatEvent event) {
    LPCPlugin.core().getLogger().info("paper chat event");
    event.renderer((source, sourceDisplayName, message, viewer) ->
        LPCPlugin.core().formatChat(source, PlainTextComponentSerializer.plainText().serialize(message))
    );
  }
}
