// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.client;

import com.ced.disabilities.DisabilityManager;
import com.ced.disabilities.DisabilityMod;
import com.ced.disabilities.disability.Disabilities;
import com.ced.disabilities.disability.Disability;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.List;

public class DisabilityClient implements ClientModInitializer {
	private static boolean showList = true;

	private static final java.util.Random RANDOM = new java.util.Random();

	private static Double savedVolume;

	private static void selftest(Minecraft mc) {
		if (!Boolean.getBoolean("disabilitystack.selftest") || mc.player == null) return;
		java.nio.file.Path f = java.nio.file.Path.of("selftest.cmd");
		String line;
		try {
			if (!java.nio.file.Files.exists(f)) return;
			line = java.nio.file.Files.readString(f).trim();
			java.nio.file.Files.delete(f);
		} catch (java.io.IOException e) {
			return;
		}
		String[] a = line.split(" ");
		switch (a[0]) {
			case "chat" -> mc.player.connection.sendChat(line.substring(5));
			case "cmd" -> mc.player.connection.sendCommand(line.substring(4));
			case "sprint" -> {
				boolean on = a[1].equals("on"); forceSprint = on;
				mc.options.keyUp.setDown(on);
				mc.options.keySprint.setDown(on);
			}
			case "hkey" -> net.minecraft.client.KeyMapping.click(com.mojang.blaze3d.platform.InputConstants.Type.KEYBOARD.getOrCreate(com.mojang.blaze3d.platform.InputConstants.KEY_H));
			case "screen" -> mc.gui.openChatScreen(net.minecraft.client.gui.components.ChatComponent.ChatMethod.MESSAGE);
			case "cam" -> mc.options.setCameraType(a[1].equals("3") ? net.minecraft.client.CameraType.THIRD_PERSON_BACK : net.minecraft.client.CameraType.FIRST_PERSON);
			case "shot" -> net.minecraft.client.Screenshot.grab(mc, false);
			case "respawn" -> mc.player.respawn();
			case "drop" -> net.minecraft.client.KeyMapping.click(mc.options.keyDrop.getDefaultKey());
			case "setvol" -> mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(Double.parseDouble(a[1]));
			case "keyuse" -> mc.options.keyUse.setDown(a[1].equals("on"));
			case "use" -> mc.gameMode.useItem(mc.player, a[1].equals("off")
					? net.minecraft.world.InteractionHand.OFF_HAND : net.minecraft.world.InteractionHand.MAIN_HAND);
			case "bed" -> {
				var pos = new net.minecraft.core.BlockPos(Integer.parseInt(a[1]), Integer.parseInt(a[2]), Integer.parseInt(a[3]));
				mc.gameMode.useItemOn(mc.player, net.minecraft.world.InteractionHand.MAIN_HAND,
						new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),
								net.minecraft.core.Direction.UP, pos, false));
			}
			case "report" -> {
				float y0 = mc.player.getYRot();
				double vol = mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).get();
				try {
					java.nio.file.Files.writeString(java.nio.file.Path.of("selftest.out"),
							"vol=" + vol + " sprinting=" + mc.player.isSprinting() + " hp=" + mc.player.getHealth() + " yaw=" + y0 + " countdown=" + remainingTicks(mc) + " barMax=" + barMax
										+ " mouse=" + (int) mc.mouseHandler.getScaledXPos(mc.getWindow()) + "," + (int) mc.mouseHandler.getScaledYPos(mc.getWindow())
										+ " showList=" + showList + " collapsed=" + collapsed + " gui=" + mc.getWindow().getGuiScaledWidth() + "x" + mc.getWindow().getGuiScaledHeight()
										+ " win=" + mc.getWindow().getWidth() + "x" + mc.getWindow().getHeight() + "\n",
							java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
				} catch (java.io.IOException ignored) {
				}
			}
			default -> {
			}
		}
	}

	private static void tickEffects(Minecraft mc) {
		selftest(mc);
		if (forceSprint && mc.player != null) { mc.player.setSprinting(true); }
		List<String> owned = mc.player == null ? List.of()
				: mc.player.getAttachedOrElse(DisabilityManager.OWNED, List.of());
		var master = mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER);
		if (master.get() != lastMaster) { dbg("master " + lastMaster + " -> " + master.get() + " owned=" + owned); lastMaster = master.get(); }
		if (owned.contains("deaf")) {
			if (savedVolume == null) { savedVolume = master.get(); writeVolumeFile(savedVolume); dbg("deaf apply saved=" + savedVolume + " owned=" + owned); }
			if (master.get() != 0.0) master.set(0.0);
		} else {
			restoreVolume(mc);
		}
		hideNameTags(mc, owned.contains("prosopagnosia"));
		tremor(mc, owned.contains("parkinsons"));
	}

	private static int tremorLeft, tremorWait = 400;

	private static void tremor(Minecraft mc, boolean on) {
		if (!on || mc.player == null || mc.gui.screen() != null || !mc.player.isAlive()) { tremorLeft = 0; return; }
		if (tremorLeft > 0) {
			tremorLeft--;
			mc.player.turn((RANDOM.nextDouble() - 0.5) * 40, (RANDOM.nextDouble() - 0.5) * 30);
			if (tremorLeft == 0) tremorWait = 400 + RANDOM.nextInt(800);
			return;
		}
		if (--tremorWait <= 0) { tremorLeft = 60 + RANDOM.nextInt(80); dbg("parkinsons episode " + tremorLeft); return; }
		if (RANDOM.nextInt(3) == 0) mc.player.turn((RANDOM.nextDouble() - 0.5) * 6, (RANDOM.nextDouble() - 0.5) * 6);
	}

	private static final String FACE_TEAM = "dsfaceblind";
	private static int hiddenNames;

	private static void hideNameTags(Minecraft mc, boolean on) {
		if (mc.level == null || mc.getConnection() == null) return;
		var sb = mc.level.getScoreboard();
		var team = sb.getPlayerTeam(FACE_TEAM);
		if (!on) {
			if (team != null) sb.removePlayerTeam(team);
			hiddenNames = 0;
			return;
		}
		if (team == null) {
			team = sb.addPlayerTeam(FACE_TEAM);
			team.setNameTagVisibility(net.minecraft.world.scores.Team.Visibility.NEVER);
		}
		for (var info : mc.getConnection().getOnlinePlayers()) {
			String name = info.getProfile().name();
			if (name.equals(mc.player.getGameProfile().name()) || sb.getPlayersTeam(name) != null) continue;
			sb.addPlayerToTeam(name, team);
		}
		hiddenNames = team.getPlayers().size();
	}

	private static double lastMaster = -1;
	private static boolean forceSprint;

	private static void dbg(String m) {
		if (!Boolean.getBoolean("disabilitystack.selftest")) return;
		try {
			java.nio.file.Files.writeString(java.nio.file.Path.of("selftest.out"), "dbg " + m + "\n", java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
		} catch (java.io.IOException ignored) {
		}
	}

	private static void restoreVolume(Minecraft mc) {
		if (savedVolume == null) return;
		dbg("restore to " + savedVolume);
		mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(savedVolume);
		savedVolume = null;
		try { java.nio.file.Files.deleteIfExists(VOLUME_FILE); } catch (java.io.IOException ignored) { }
	}

	private static final java.nio.file.Path VOLUME_FILE = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("mors-disability-volume.txt");

	private static void writeVolumeFile(double v) {
		try { java.nio.file.Files.writeString(VOLUME_FILE, Double.toString(v)); } catch (java.io.IOException ignored) { }
	}

	private static void recoverVolume(Minecraft mc) {
		try {
			if (!java.nio.file.Files.exists(VOLUME_FILE)) return;
			savedVolume = Double.parseDouble(java.nio.file.Files.readString(VOLUME_FILE).trim());
			restoreVolume(mc);
		} catch (java.io.IOException | RuntimeException ignored) { }
	}

	@Override
	public void onInitializeClient() {
		if (!Boolean.getBoolean("disabilitystack.nohello")) {
			net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking.registerGlobalReceiver(com.ced.disabilities.ModCheck.Hello.TYPE, (payload, context) -> {});
		}
		KeyMapping toggle = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.mors-disability.toggle_list", InputConstants.Type.KEYBOARD, InputConstants.KEY_H,
				KeyMapping.Category.register(Identifier.fromNamespaceAndPath(DisabilityMod.MOD_ID, "main"))));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggle.consumeClick()) showList = !showList;
			tickEffects(client);
			Hallucination.tick(client);
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> restoreVolume(client));
		ClientLifecycleEvents.CLIENT_STOPPING.register(DisabilityClient::restoreVolume);
		ClientLifecycleEvents.CLIENT_STARTED.register(DisabilityClient::recoverVolume);

		ScreenEvents.AFTER_INIT.register((client, screen, sw, sh) -> ScreenMouseEvents.allowMouseClick(screen).register((s, ev) -> {
			if (!showList || ev.button() != 0 || ev.x() < 4 || ev.x() >= 4 + headW || ev.y() < 4 || ev.y() >= 18) return true;
			collapsed = !collapsed;
			return false;
		}));

		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
				Identifier.fromNamespaceAndPath(DisabilityMod.MOD_ID, "list"), (graphics, tickCounter) -> drawHud(graphics));
	}

	private static long clientTick;
	private static int lastSynced = -1;
	private static long lastSyncedAt;
	private static int barMax = 1;
	private static java.util.Map<String, Integer> lastView = java.util.Map.of();
	private static long viewAt;

	private static int remainingTicks(Minecraft mc) {
		clientTick = mc.player.tickCount;
		int v = mc.player.getAttachedOrElse(DisabilityManager.COUNTDOWN, -1);
		if (v < 0) {
			lastSynced = -1;
			return -1;
		}
		int predicted = lastSynced < 0 ? -1 : lastSynced - (int) (clientTick - lastSyncedAt);
		if (v != lastSynced) {
			if (lastSynced < 0 || v > predicted + 20) barMax = v;
			barMax = Math.max(barMax, v);
			lastSynced = v;
			lastSyncedAt = clientTick;
		}
		return Math.max(0, lastSynced - (int) (clientTick - lastSyncedAt));
	}

	private static int badgeColor(String id) {
		float h = (id.hashCode() & 0x7fffffff) % 360 / 360f;
		return 0xFF000000 | java.awt.Color.HSBtoRGB(h, 0.55f, 0.6f);
	}

	private static final int ROW_H = 12;
	private static boolean collapsed;
	private static int headW;

	private static void drawHud(GuiGraphicsExtractor g) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		List<String> owned = mc.player.getAttachedOrElse(DisabilityManager.OWNED, List.of());
		if (owned.contains("color_blind")) {
			g.fill(0, 0, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), 0x66808080);
		}
		if (!showList || owned.contains("anosognosia")) return;
		var view = mc.player.getAttachedOrElse(DisabilityManager.TIMER_VIEW, java.util.Map.<String, Integer>of());
		if (!view.equals(lastView)) { lastView = view; viewAt = mc.player.tickCount; }
		var font = mc.font;

		int x = 4, y = 4;
		String title = (collapsed ? "[+] " : "[-] ") + "Disabilities " + owned.size();
		int w = font.width(title);
		if (!collapsed) {
			for (String id : owned) {
				Disability d = Disabilities.get(id);
				w = Math.max(w, 18 + font.width(d != null ? d.displayName : id) + 34);
			}
		}
		w += 10;
		int headH = 14;
		Integer nextLeft = owned.isEmpty() ? view.get("_next") : null;
		if (nextLeft != null && !collapsed) w = Math.max(w, font.width("Next disability 00:00") + 10);
		int h = headH + 2 + (collapsed ? 0 : Math.max(owned.size(), nextLeft != null ? 1 : 0) * ROW_H) + 2;

		boolean cursorFree = mc.gui.screen() != null;
		int mx = cursorFree ? (int) mc.mouseHandler.getScaledXPos(mc.getWindow()) : -1;
		int my = cursorFree ? (int) mc.mouseHandler.getScaledYPos(mc.getWindow()) : -1;
		boolean overHead = mx >= x && mx < x + w && my >= y && my < y + headH;
		headW = w;

		g.fill(x, y, x + w, y + h, 0xB0141420);
		g.outline(x, y, w, h, 0xFF5A5A78);
		if (overHead) g.fill(x + 1, y + 1, x + w - 1, y + headH, 0x30FFFFFF);
		g.text(font, title, x + 5, y + 3, 0xFFFFFFFF, true);
		if (collapsed) return;

		Disability hovered = null;
		int ry = y + headH + 2;
		if (nextLeft != null) {
			int secs = Math.max(0, nextLeft - (int) (mc.player.tickCount - viewAt)) / 20;
			String t = String.format("%d:%02d", secs / 60, secs % 60);
			g.text(font, "Next disability", x + 5, ry + 2, 0xFFFFFFFF, true);
			g.text(font, t, x + w - 5 - font.width(t), ry + 2, 0xFF9090B0, true);
		}
		for (String id : owned) {
			Disability d = Disabilities.get(id);
			boolean over = mx >= x && mx < x + w && my >= ry && my < ry + ROW_H;
			if (over) g.fill(x + 1, ry, x + w - 1, ry + ROW_H, 0x40FFFFFF);
			g.fill(x + 3, ry + 1, x + 13, ry + 11, badgeColor(id));
			g.blit(RenderPipelines.GUI_TEXTURED,
					Identifier.fromNamespaceAndPath(DisabilityMod.MOD_ID, "textures/gui/disability/" + id + ".png"),
					x + 3, ry + 1, 0f, 0f, 10, 10, 16, 16, 16, 16);
			g.text(font, d != null ? d.displayName : id, x + 17, ry + 2, 0xFFFFFFFF, true);
			Integer left = view.get(id);
			if (left != null) {
				int elapsed = (int) (mc.player.tickCount - viewAt);
				int rem = Math.max(0, left - elapsed);
				if (rem == 0) for (int v : view.values()) rem = Math.max(rem, v - elapsed);
				int secs = rem / 20;
				String t = String.format("%d:%02d", secs / 60, secs % 60);
				g.text(font, t, x + w - 5 - font.width(t), ry + 2, 0xFF9090B0, true);
			}
			if (over && d != null) hovered = d;
			ry += ROW_H;
		}
		if (hovered != null) {
			int tw = font.width(hovered.description) + 8;
			int tx = Math.min(mx + 10, mc.getWindow().getGuiScaledWidth() - tw - 2), ty = my + 10;
			g.fill(tx, ty, tx + tw, ty + 16, 0xF0100010);
			g.outline(tx, ty, tw, 16, 0xFF5000A0);
			g.text(font, hovered.description, tx + 4, ty + 4, 0xFFFFFFFF, true);
		}
	}
}