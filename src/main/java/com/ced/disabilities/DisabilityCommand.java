// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities;

import com.ced.disabilities.disability.Disabilities;
import com.ced.disabilities.disability.Disability;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

public final class DisabilityCommand {
	private static final SuggestionProvider<CommandSourceStack> IDS =
			(ctx, builder) -> SharedSuggestionProvider.suggest(Disabilities.ids(), builder);

	private DisabilityCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> d) {
		d.register(Commands.literal("disability")
				.requires(src -> src.hasPermission(2))
				.then(Commands.literal("voicetest").executes(ctx -> voiceTest(ctx)))
				.then(Commands.literal("give").then(Commands.argument("targets", EntityArgument.players())
						.then(Commands.argument("id", StringArgumentType.word()).suggests(IDS).executes(ctx -> give(ctx)))))
				.then(Commands.literal("remove").then(Commands.argument("targets", EntityArgument.players())
						.then(Commands.argument("id", StringArgumentType.word()).suggests(IDS).executes(ctx -> remove(ctx)))))
				.then(Commands.literal("list").executes(ctx -> list(ctx, List.of(ctx.getSource().getPlayerOrException())))
						.then(Commands.argument("targets", EntityArgument.players())
								.executes(ctx -> list(ctx, EntityArgument.getPlayers(ctx, "targets")))))
				.then(Commands.literal("next").executes(ctx -> next(ctx, List.of(ctx.getSource().getPlayerOrException())))
						.then(Commands.argument("targets", EntityArgument.players())
								.executes(ctx -> next(ctx, EntityArgument.getPlayers(ctx, "targets")))))
				.then(Commands.literal("reset").executes(ctx -> reset(ctx, List.of(ctx.getSource().getPlayerOrException())))
						.then(Commands.argument("targets", EntityArgument.players())
								.executes(ctx -> reset(ctx, EntityArgument.getPlayers(ctx, "targets")))))
				.then(Commands.literal("reload").executes(DisabilityCommand::reload))
				.then(timerNode())
				.then(Commands.literal("mode")
						.then(Commands.literal("stack").executes(ctx -> mode(ctx, "stack")))
						.then(Commands.literal("replace").executes(ctx -> mode(ctx, "replace")))));
	}

	/** Dev-only (selftest flag): exercises the Simple Voice Chat hooks for the caller. */
	private static int voiceTest(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		if (!Boolean.getBoolean("disabilitystack.selftest")
				|| !net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("voicechat")) return 0;
		ServerPlayer p = ctx.getSource().getPlayerOrException();
		try {
			com.ced.disabilities.voice.VoicechatIntegration.selfTest(p);
			com.ced.disabilities.voice.VoiceFx.whisper(p);
			com.ced.disabilities.voice.VoiceFx.tic(p);
		} catch (Throwable t) {
			DisabilityMod.LOGGER.error("[selftest] voicetest failed", t);
			return 0;
		}
		return 1;
	}

	private static int give(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		Disability disability = byId(ctx);
		if (disability == null) return 0;
		int n = 0;
		for (ServerPlayer p : EntityArgument.getPlayers(ctx, "targets")) {
			if (DisabilityManager.give(p, disability)) n++;
		}
		int count = n;
		ctx.getSource().sendSuccess(() -> Component.literal("Gave " + disability.displayName + " to " + count + " player(s)"), true);
		return n;
	}

	private static int remove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		Disability disability = byId(ctx);
		if (disability == null) return 0;
		int n = 0;
		for (ServerPlayer p : EntityArgument.getPlayers(ctx, "targets")) {
			if (DisabilityManager.remove(p, disability)) n++;
		}
		int count = n;
		ctx.getSource().sendSuccess(() -> Component.literal("Removed " + disability.displayName + " from " + count + " player(s)"), true);
		return n;
	}

	private static int list(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) {
		for (ServerPlayer p : targets) {
			List<String> owned = DisabilityManager.owned(p);
			ctx.getSource().sendSuccess(() -> Component.literal(p.getGameProfile().getName() + ": "
					+ (owned.isEmpty() ? "none" : String.join(", ", owned))), false);
		}
		return targets.size();
	}

	private static int next(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) {
		int n = 0;
		for (ServerPlayer p : targets) {
			if (DisabilityManager.giveRandom(p) != null) n++;
			DisabilityManager.resetTimers(p);
		}
		int count = n;
		ctx.getSource().sendSuccess(() -> Component.literal("Gave a new disability to " + count + " player(s)"), true);
		return n;
	}

	private static int reset(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) {
		for (ServerPlayer p : targets) DisabilityManager.reset(p);
		ctx.getSource().sendSuccess(() -> Component.literal("Reset " + targets.size() + " player(s)"), true);
		return targets.size();
	}

	private static int reload(CommandContext<CommandSourceStack> ctx) {
		DisabilityConfig.load();
		ctx.getSource().sendSuccess(() -> Component.literal("Config reloaded, mode = " + DisabilityConfig.get().mode + ", interval = "
				+ DisabilityConfig.get().intervalMinutes + " min"), true);
		return 1;
	}

	private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> timerNode() {
		var node = Commands.literal("timer").then(Commands.literal("off").executes(ctx -> timer(ctx, 0)));
		for (String sev : Disabilities.SEVERITIES) {
			node.then(Commands.literal(sev)
					.then(Commands.literal("off").executes(ctx -> severityTimer(ctx, sev, 0)))
					.then(Commands.argument("minutes", DoubleArgumentType.doubleArg(0.05))
							.executes(ctx -> severityTimer(ctx, sev, DoubleArgumentType.getDouble(ctx, "minutes")))));
		}
		return node.then(Commands.literal("shared").executes(ctx -> sharedTimer(ctx)))
				.then(Commands.argument("minutes", DoubleArgumentType.doubleArg(0.05))
						.executes(ctx -> timer(ctx, DoubleArgumentType.getDouble(ctx, "minutes"))));
	}

	private static int timer(CommandContext<CommandSourceStack> ctx, double minutes) {
		DisabilityConfig.get().intervalMinutes = minutes;
		DisabilityConfig.get().severityMinutes.clear();
		DisabilityConfig.save();
		ctx.getSource().sendSuccess(() -> Component.literal(minutes <= 0 ? "Timer off"
				: "Timer set to " + minutes + " min (saved to config)"), true);
		return 1;
	}

	private static int severityTimer(CommandContext<CommandSourceStack> ctx, String s, double minutes) {
		if (!Disabilities.SEVERITIES.contains(s)) {
			ctx.getSource().sendFailure(Component.literal("Severity must be one of " + Disabilities.SEVERITIES));
			return 0;
		}
		DisabilityConfig cfg = DisabilityConfig.get();
		for (String k : Disabilities.SEVERITIES) cfg.severityMinutes.putIfAbsent(k, cfg.intervalMinutes);
		cfg.severityMinutes.put(s, minutes);
		DisabilityConfig.save();
		ctx.getSource().sendSuccess(() -> Component.literal(s + " timer " + (minutes <= 0 ? "off" : "set to " + minutes + " min") + " (saved to config)"), true);
		return 1;
	}

	private static int sharedTimer(CommandContext<CommandSourceStack> ctx) {
		DisabilityConfig.get().severityMinutes.clear();
		DisabilityConfig.save();
		ctx.getSource().sendSuccess(() -> Component.literal("One shared timer again (" + DisabilityConfig.get().intervalMinutes + " min)"), true);
		return 1;
	}

	private static int mode(CommandContext<CommandSourceStack> ctx, String mode) {
		DisabilityConfig.get().mode = mode;
		DisabilityConfig.save();
		ctx.getSource().sendSuccess(() -> Component.literal("Timer mode: " + mode), true);
		return 1;
	}

	private static Disability byId(CommandContext<CommandSourceStack> ctx) {
		String id = StringArgumentType.getString(ctx, "id");
		Disability d = Disabilities.get(id);
		if (d == null) ctx.getSource().sendFailure(Component.literal("Unknown disability: " + id));
		return d;
	}
}