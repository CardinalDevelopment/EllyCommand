package ee.ellytr.command.argument.provider.minecraft;

import com.google.common.collect.Lists;
import ee.ellytr.command.argument.ArgumentProvider;
import ee.ellytr.command.util.Strings;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class BiomeProvider implements ArgumentProvider<Biome> {

  @Override
  public Biome getMatch(String in, Player player) {
    return Biome.valueOf(Strings.getTechnicalName(in));
  }

  @Override
  public List<String> getSuggestions(String in, Player player) {
    return Lists.newArrayList(Biome.values()).stream().map(Enum::name)
        .filter(str -> str.toLowerCase().startsWith(in.toLowerCase())).collect(Collectors.toList());
  }

}
