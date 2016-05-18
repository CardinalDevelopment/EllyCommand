package ee.ellytr.command.argument.provider;

import ee.ellytr.command.argument.ArgumentProvider;
import org.bukkit.entity.Player;

import java.util.List;

public class ByteProvider implements ArgumentProvider<Byte> {

  @Override
  public Byte getMatch(String in, Player player) {
    return Byte.parseByte(in);
  }

  @Override
  public List<String> getSuggestions(String in, Player player) {
    return null;
  }

}
