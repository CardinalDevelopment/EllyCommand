package ee.ellytr.command.util;

import com.sun.deploy.util.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;

public class Commands {

  public static CommandMap getCommandMap() {
    return Reflections.getField(Bukkit.getPluginManager(), "commandMap");
  }

}
