package ee.ellytr.command;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class CommandRegistry {

  private final Plugin plugin;

  private final List<Class> classes = Lists.newArrayList();
  private final CommandFactory factory = new CommandFactory(this);
  private final ProviderRegistry providerRegistry = new ProviderRegistry();

  public void addClass(Class clazz) {
    classes.add(clazz);
  }

  public void removeClass(Class clazz) {
    classes.remove(clazz);
  }

  public void register() {
    factory.build();
  }

}
