package ee.ellytr.command.argument.provider;

import com.google.common.collect.Lists;
import ee.ellytr.command.argument.ArgumentProvider;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class BooleanProvider implements ArgumentProvider<Boolean> {

  @Override
  public Boolean getMatch(String in, Player player) {
    return in.equalsIgnoreCase("true")
        || in.equalsIgnoreCase("on");
  }

  @Override
  public List<String> getSuggestions(String in, Player player) {
    return Lists.newArrayList("true", "false").stream()
        .filter(str -> str.toLowerCase().startsWith(in.toLowerCase())).collect(Collectors.toList());
  }

}
