// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities.disability;

import com.ced.disabilities.DisabilityMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class OcdDisability extends Disability {
	private static final Random RANDOM = new Random();

	public OcdDisability() {
		super("ocd", "OCD", "Compulsions: you keep sorting your inventory");
	}

	@Override
	public void tick(ServerPlayer player) {
		if (RANDOM.nextInt(odds(3600)) != 0) return;
		List<ItemStack> stacks = new ArrayList<>();
		for (int i = 9; i < 36; i++) {
			ItemStack s = player.getInventory().getItem(i);
			if (!s.isEmpty()) stacks.add(s.copy());
		}
		stacks.sort(Comparator.comparing((ItemStack s) -> BuiltInRegistries.ITEM.getKey(s.getItem()).toString()));
		for (int i = 9; i < 36; i++) {
			int k = i - 9;
			player.getInventory().setItem(i, k < stacks.size() ? stacks.get(k) : ItemStack.EMPTY);
		}
		if (Boolean.getBoolean("disabilitystack.selftest")) DisabilityMod.LOGGER.info("[selftest] ocd sorted {} stacks", stacks.size());
	}
}
