// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import com.ced.disabilities.DisabilityManager;
import com.ced.disabilities.DisabilityMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import java.lang.reflect.Field;
import java.util.Map;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.Component;

import java.util.List;

public class MuteDisability extends Disability {
	public MuteDisability() {
		super("mute", "Mute", "Loss of speech: you cannot speak in chat");
	}

	private static boolean muted(net.minecraft.world.entity.Entity e) {
		return e != null && e.getAttachedOrElse(DisabilityManager.OWNED, List.<String>of()).contains("mute");
	}

	private static final String[] SPEECH_COMMANDS = {"say", "me", "msg", "tell", "w", "teammsg", "tm"};

	@SuppressWarnings("unchecked")
	public static void guardCommands(CommandDispatcher<CommandSourceStack> d) {
		SimpleCommandExceptionType mutedError = new SimpleCommandExceptionType(Component.literal("You can't speak."));
		try {
			Field children = CommandNode.class.getDeclaredField("children");
			Field literals = CommandNode.class.getDeclaredField("literals");
			children.setAccessible(true);
			literals.setAccessible(true);
			for (String name : SPEECH_COMMANDS) {
				CommandNode<CommandSourceStack> original = d.getRoot().getChild(name);
				if (original == null) continue;
				((Map<String, ?>) children.get(d.getRoot())).remove(name);
				((Map<String, ?>) literals.get(d.getRoot())).remove(name);

				CommandNode<CommandSourceStack> target = original;
				while (target.getRedirect() != null) target = target.getRedirect();
				d.register(Commands.literal(name).redirect(target, ctx -> {
					if (muted(ctx.getSource().getEntity())) throw mutedError.create();
					return ctx.getSource();
				}));
			}
		} catch (ReflectiveOperationException e) {
			DisabilityMod.LOGGER.error("Could not guard speech commands for Mute", e);
		}
	}

	public static void registerEvents() {

		ServerMessageEvents.ALLOW_COMMAND_MESSAGE.register((message, source, params) -> {
			if (!muted(source.getEntity())) return true;
			source.sendSystemMessage(Component.literal("You can't speak."));
			return false;
		});
		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
			if (sender.getAttachedOrElse(DisabilityManager.OWNED, List.<String>of()).contains("mute")) {
				sender.sendSystemMessage(Component.literal("You can't speak."), true);
				return false;
			}
			return true;
		});
	}
}