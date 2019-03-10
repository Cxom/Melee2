package me.cxom.melee2.messaging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.cxom.melee2.Melee;

public class Messaging {

	private static enum Language {
		ENGLISH
	}
	
	public static class Message {
		
		private static final Language DEFAULT_LANGUAGE = Language.ENGLISH;
		
		private Map<Language, String> translations;
		
		public Message(Map<Language, String> translations) {
			this.translations = translations;
		}
		
		public String get(Object... arguments) {
			return get(DEFAULT_LANGUAGE, arguments);
		}
		
		public String get(Language language, Object... arguments) {
			String translationWithPlaceholders = translations.get(language);
			return String.format(translationWithPlaceholders, arguments);
		}
	
	}
	
	public static void send(Player player, Message message, Object... arguments) {
		player.sendMessage(Melee.MELEE_CHAT_PREFIX + message.get(arguments));
		// TODO i18n of strings - not hardcoded
	}
	
	public static void broadcast(Collection<Player> players, Message message, Object... arguments) {
		players.forEach((player) -> send(player, message, arguments));
	} 
	
	public static final Message ERROR_GAME_STOPPED = new Message(mapOf(Language.ENGLISH, ChatColor.RED + "This game has been stopped, you cannot join."));

	public static final Message ERROR_ALREADY_IN_LOBBY = new Message(mapOf(Language.ENGLISH, ChatColor.RED + "You are already in the lobby!"));
	
	public static final Message LOBBY_ENOUGH_PLAYERS_READY_UP = new Message(mapOf(Language.ENGLISH, ChatColor.GREEN + "Enough players in the lobby, ready up to start the countdown!"));
	
	public static final Message LOBBY_START_CONDITIONS_NO_LONGER_MET = new Message(mapOf(Language.ENGLISH, ChatColor.RED + "Not enough players in lobby and ready, start aborted!"));
		
	// First parameter - lobby name
	public static final Message LOBBY_REMOVING_YOU_FROM = new Message(mapOf(Language.ENGLISH, ChatColor.RED + "" + ChatColor.ITALIC + "Removing you from %s lobby . . ."));
	
	public static final Message GAME_INTERRUPTED = new Message(mapOf(Language.ENGLISH, ChatColor.RED + "The game has been interrupted. Stopping . . ."));
	
	public static void MATCH_STARTING_IN(Player player, int seconds, String arenaName) {
		player.sendMessage(Melee.MELEE_CHAT_PREFIX + ChatColor.GOLD + "Match starting in " + seconds + " second(s) on " + arenaName + "!");
	}
	
	private static Map<Language, String> mapOf(Language l, String s){
		Map<Language, String> map = new HashMap<>();
		map.put(l,  s);
		return map;
	}
	
}
