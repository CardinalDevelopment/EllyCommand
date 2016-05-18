package ee.ellytr.command.argument.provider;

import ee.ellytr.command.argument.ArgumentProvider;
import org.bukkit.entity.Player;

import java.util.List;

public class DoubleProvider implements ArgumentProvider<Double> {

  @Override
  public Double getMatch(String in, Player player) {
    return Double.parseDouble(in);
  }

  @Override
  public List<String> getSuggestions(String in, Player player) {
    return null;
  }

}
