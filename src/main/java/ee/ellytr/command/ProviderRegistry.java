package ee.ellytr.command;

import ee.ellytr.command.argument.ArgumentProvider;
import ee.ellytr.command.argument.provider.BooleanProvider;
import ee.ellytr.command.argument.provider.ByteProvider;
import ee.ellytr.command.argument.provider.CharacterProvider;
import ee.ellytr.command.argument.provider.DoubleProvider;
import ee.ellytr.command.argument.provider.FloatProvider;
import ee.ellytr.command.argument.provider.IntegerProvider;
import ee.ellytr.command.argument.provider.LongProvider;
import ee.ellytr.command.argument.provider.ShortProvider;
import ee.ellytr.command.argument.provider.StringProvider;
import ee.ellytr.command.argument.provider.minecraft.BiomeProvider;
import ee.ellytr.command.argument.provider.minecraft.PlayerProvider;
import ee.ellytr.command.argument.provider.minecraft.WorldProvider;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.Map;

public class ProviderRegistry {

  private Map<Class, ArgumentProvider> providers;

  protected ProviderRegistry() {
    registerDefaultProviders();
  }

  public <T> void registerProvider(Class<T> clazz, ArgumentProvider<T> provider) {
    providers.put(clazz, provider);
  }

  // ArgumentProviders will always match up with their respective class,
  // so we do not need to have an unchecked cast warning.
  @SuppressWarnings("unchecked")
  public <T> ArgumentProvider<T> getProvider(Class<T> clazz) {
    return providers.get(clazz);
  }

  private void registerDefaultProviders() {
    registerProvider(Boolean.class, new BooleanProvider());
    registerProvider(Byte.class, new ByteProvider());
    registerProvider(Character.class, new CharacterProvider());
    registerProvider(Double.class, new DoubleProvider());
    registerProvider(Float.class, new FloatProvider());
    registerProvider(Integer.class, new IntegerProvider());
    registerProvider(Long.class, new LongProvider());
    registerProvider(Short.class, new ShortProvider());
    registerProvider(String.class, new StringProvider());

    registerProvider(Biome.class, new BiomeProvider());
    registerProvider(Player.class, new PlayerProvider());
    registerProvider(World.class, new WorldProvider());
  }

}
