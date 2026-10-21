package com.ced.disabilities.disability;

import com.ced.disabilities.DisabilityManager;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** The letters inside every word of the player's chat messages get scrambled. */
public class DyslexiaDisability extends Disability {
	private static final Random RANDOM = new Random();

	public DyslexiaDisability() {
		super("dyslexia", "Dyslexia", "Letters in your chat messages get scrambled");
	}

	static String scramble(String text) {
		StringBuilder out = new StringBuilder();
		boolean first = true;
		for (String word : text.split(" ", -1)) {
			if (!first) out.append(' ');
			first = false;
			if (word.length() > 3) {
				List<Character> mid = new ArrayList<>();
				for (char c : word.substring(1, word.length() - 1).toCharArray()) mid.add(c);
				Collections.shuffle(mid, RANDOM);
				out.append(word.charAt(0));
				for (char c : mid) out.append(c);
				out.append(word.charAt(word.length() - 1));
			} else {
				out.append(word);
			}
		}
		return out.toString();
	}

	public static void registerEvents() {
		ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
			List<String> owned = sender.getAttachedOrElse(DisabilityManager.OWNED, List.<String>of());
			if (!owned.contains("dyslexia") || owned.contains("mute")) return true;
			sender.level().getServer().getPlayerList().broadcastSystemMessage(
					Component.literal("<" + sender.getGameProfile().name() + "> " + scramble(message.signedContent())), false);
			return false;
		});
	}
}
