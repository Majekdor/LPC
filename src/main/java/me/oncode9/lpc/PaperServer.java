package me.oncode9.lpc;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.oncode9.lpc.LPCPlugin;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Handles Paper server software.
 */
public final class PaperServer implements Listener {

  @EventHandler
  public void onChat(final AsyncChatEvent event) {
    event.renderer((source, sourceDisplayName, message, viewer) ->
        LPCPlugin.core().formatChat(source, PlainTextComponentSerializer.plainText().serialize(message))
    );
  }
}
