package ee.ellytr.command.argument.provider;

import ee.ellytr.command.argument.ArgumentProvider;
import org.bukkit.entity.Player;

import java.util.List;

public class ShortProvider implements ArgumentProvider<Short> {

  @Override
  public Short getMatch(String in, Player player) {
    return Short.parseShort(in);
  }

  @Override
  public List<String> getSuggestions(String in, Player player) {
    return null;
  }

}
