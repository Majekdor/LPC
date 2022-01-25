package me.oncode9.lpc;

import dev.majek.chattools.MiniMessageWrapper;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.placeholder.Placeholder;
import net.kyori.adventure.text.minimessage.placeholder.PlaceholderResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class LPCPlugin extends JavaPlugin {

	private static LPCPlugin core;

	public LPCPlugin() {
		core = this;
	}

	public static LPCPlugin core() {
		return core;
	}

	@Override
	public void onEnable() {
		this.saveDefaultConfig();

		if (this.isPlaceholderAPIEnabled())
			getLogger().info("Hooked into PlaceholderAPI.");

		if (this.isPaperServer()) {
			this.getServer().getPluginManager().registerEvents(new PaperServer(), this);
		} else {
			this.getServer().getPluginManager().registerEvents(new SpigotServer(), this);
		}
	}

	@Override
	public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command,
													 final @NotNull String label, final String[] args) {
		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			reloadConfig();

			sender.sendMessage("The configuration file has been reloaded.");
			return true;
		}

		return false;
	}

	@Override
	public List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command,
																		final @NotNull String alias, final String[] args) {
		if (args.length == 1)
			return Collections.singletonList("reload");

		return new ArrayList<>();
	}

	/**
	 * Instance of the {@link LegacyComponentSerializer} used by server implementations.
	 */
	public static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder()
			.hexColors().useUnusualXRepeatedCharacterHexFormat().character('§').build();

	/**
	 * Format the chat for all server implementations.
	 *
	 * @param source the chatter
	 * @param message the message
	 * @return formatted chat
	 */
	@SuppressWarnings("deprecation")
	public @NotNull Component formatChat(final @NotNull Player source, final @NotNull String message) {
		final FileConfiguration config = LPCPlugin.core().getConfig();
		final String group = loadUser(source).getPrimaryGroup();

		String format = config.getString(
				config.getString("group-formats." + group) != null ? "group-formats." + group : "chat-format",
				"<prefix> <name><white>: <message>"
		).replace("<world>", source.getWorld().getName());

		final MiniMessageWrapper legacyWrapper = MiniMessageWrapper.legacy();
		final MiniMessageWrapper playerWrapper = MiniMessageWrapper.builder()
				.gradients(source.hasPermission("lpc.gradients"))
				.hexColors(source.hasPermission("lpc.rgbcodes"))
				.legacyColors(source.hasPermission("lpc.colorcodes"))
				.standardColors(source.hasPermission("lpc.colorcodes"))
				.build();

		final PlaceholderResolver.Builder placeholderBuilder = PlaceholderResolver.builder().placeholders(
				Placeholder.component("prefix", legacyWrapper.mmParse(getPrefix(source))),
				Placeholder.component("prefixes", legacyWrapper.mmParse(getPrefixes(source))),
				Placeholder.component("suffix", legacyWrapper.mmParse(getSuffix(source))),
				Placeholder.component("suffixes", legacyWrapper.mmParse(getSuffixes(source))),
				Placeholder.component("message", playerWrapper.mmParse(message).style(builder -> {
					String messageColor = playerMeta(source).getMetaValue("message-color") == null
							? groupMeta(group).getMetaValue("message-color")
							: playerMeta(source).getMetaValue("message-color");
					if (messageColor != null) {
						builder.color(getColor(messageColor));
					}
				})),
				Placeholder.component("name", Component.text(source.getName()).style(builder -> {
					String usernameColor = playerMeta(source).getMetaValue("username-color") == null
							? groupMeta(group).getMetaValue("username-color")
							: playerMeta(source).getMetaValue("username-color");
					if (usernameColor != null) {
						builder.color(getColor(usernameColor));
					}
				}))
		);

		if (this.isPaperServer()) {
			placeholderBuilder.placeholder(Placeholder.component("displayname", source.displayName()));
		} else {
			placeholderBuilder.placeholder(Placeholder.component("displayname",
					LEGACY_COMPONENT_SERIALIZER.deserialize(source.getDisplayName())));
		}

		return MiniMessageWrapper.builder().legacyColors(true).placeholderResolver(placeholderBuilder.build()).build()
				.mmParse(LPCPlugin.core().isPlaceholderAPIEnabled() ? PlaceholderAPI.setPlaceholders(source, format) : format);
	}

	private @NotNull String getPrefix(final Player player) {
		final String prefix = playerMeta(player).getPrefix();

		return prefix != null ? prefix : "";
	}

	private @NotNull String getSuffix(final Player player) {
		final String suffix = playerMeta(player).getSuffix();

		return suffix != null ? suffix : "";
	}

	private @NotNull String getPrefixes(final Player player) {
		final SortedMap<Integer, String> map = playerMeta(player).getPrefixes();
		final StringBuilder prefixes = new StringBuilder();

		for (final String prefix : map.values())
			prefixes.append(prefix);

		return prefixes.toString();
	}

	private @NotNull String getSuffixes(final Player player) {
		final SortedMap<Integer, String> map = playerMeta(player).getSuffixes();
		final StringBuilder suffixes = new StringBuilder();

		for (final String prefix : map.values())
			suffixes.append(prefix);

		return suffixes.toString();
	}

	private @NotNull CachedMetaData playerMeta(final Player player) {
		return loadUser(player).getCachedData().getMetaData(getApi().getContextManager().getQueryOptions(player));
	}

	private @NotNull CachedMetaData groupMeta(final String group) {
		return loadGroup(group).getCachedData().getMetaData(getApi().getContextManager().getStaticQueryOptions());
	}

	private User loadUser(final @NotNull Player player) {
		if (!player.isOnline())
			throw new IllegalStateException("Player is offline!");

		return getApi().getUserManager().getUser(player.getUniqueId());
	}

	private Group loadGroup(final String group) {
		return getApi().getGroupManager().getGroup(group);
	}

	private @NotNull LuckPerms getApi() {
		final RegisteredServiceProvider<LuckPerms> provider = this.getServer()
				.getServicesManager().getRegistration(LuckPerms.class);
		Validate.notNull(provider);
		return provider.getProvider();
	}

	private @Nullable TextColor getColor(final @NotNull String color) {
		return MiniMessageWrapper.legacy().mmParse(color).color();
	}

	private boolean isPlaceholderAPIEnabled() {
		return getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
	}

	private boolean isPaperServer() {
		try {
			Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
			return true;
		} catch (final ClassNotFoundException ignored) {
			return false;
		}
	}
}
