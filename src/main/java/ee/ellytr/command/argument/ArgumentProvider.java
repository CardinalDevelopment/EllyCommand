package ee.ellytr.command.argument;

import org.bukkit.entity.Player;

import java.util.List;

public interface ArgumentProvider<T> {

  T getMatch(String in, Player player);

  List<String> getSuggestions(String in, Player player);

}
