package me.zombie_striker.qg.api;


import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.zombie_striker.qg.guns.Gun;

public class QAWeaponReloadEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();

	private final Player player;
	private final Gun gun;
	private boolean cancelled;

	public QAWeaponReloadEvent(Player player, Gun gun) {
		this.player = player;
		this.gun = gun;
		this.cancelled = false;
	}

	public Player getPlayer() {
		return player;
	}

	public Gun getGun() {
		return gun;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
