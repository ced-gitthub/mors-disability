// Copyright (c) 2026 Ced. MIT License.
package com.ced.disabilities;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ClientCache {
	public static volatile UUID self;
	public static volatile List<String> owned = List.of();
	public static volatile Map<String, Integer> timerView = Map.of();
	public static volatile int countdown = -1;

	private ClientCache() {}

	public static void clear() {
		self = null;
		owned = List.of();
		timerView = Map.of();
		countdown = -1;
	}
}
