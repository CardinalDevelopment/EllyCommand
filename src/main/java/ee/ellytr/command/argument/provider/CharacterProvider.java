package ee.ellytr.command.argument.provider;

import ee.ellytr.command.argument.ArgumentProvider;
import org.bukkit.entity.Player;

import java.util.List;

public class CharacterProvider implements ArgumentProvider<Character> {

  @Override
  public Character getMatch(String in, Player player) {
    return in.charAt(0);
  }

  @Override
  public List<String> getSuggestions(String in, Player player) {
    return null;
  }

}
