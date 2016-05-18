package ee.ellytr.command.argument.provider;

import ee.ellytr.command.argument.ArgumentProvider;
import org.bukkit.entity.Player;

import java.util.List;

public class FloatProvider implements ArgumentProvider<Float> {

  @Override
  public Float getMatch(String in, Player player) {
    return Float.parseFloat(in);
  }

  @Override
  public List<String> getSuggestions(String in, Player player) {
    return null;
  }

}
