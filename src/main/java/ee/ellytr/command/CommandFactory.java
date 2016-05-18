package ee.ellytr.command;

import com.google.common.collect.Lists;
import ee.ellytr.command.argument.Argument;
import ee.ellytr.command.util.Collections;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CommandFactory {

  private final CommandRegistry registry;
  private final List<EllyCommand> commands = Lists.newArrayList();

  protected void build() {
    for (Class clazz : registry.getClasses()) {
      // Builds all commands from the registry class's command methods
      commands.addAll(findCommandMethods(clazz).stream().map(method
          -> analyzeCommand(method.getDeclaredAnnotation(Command.class), method)).collect(Collectors.toList()));
    }
    for (EllyCommand command : commands) {
      for (CommandInstance instance : command.getInstances()) {
        NestedCommands nestedCommands = instance.getMethod().getDeclaredAnnotation(NestedCommands.class);
        if (nestedCommands != null) {
          for (Class clazz : nestedCommands.value()) {
            // Builds all commands from the nested class's command methods
            for (Method method : findCommandMethods(clazz)) {
              command.addNestedCommand(analyzeCommand(method.getDeclaredAnnotation(Command.class), method));
            }
          }
        }
      }
    }
    register();
  }

  private void register() {
    for (EllyCommand command : commands) {
      try {
        Constructor constructor = Class.forName(PluginCommand.class.getName()).getDeclaredConstructor(String.class, Plugin.class);
        constructor.setAccessible(true);

        Plugin plugin = registry.getPlugin();

        PluginCommand pluginCommand = (PluginCommand) constructor.newInstance(command.getName(), plugin);
        pluginCommand.setAliases(command.getAliases());
        pluginCommand.setDescription(command.getDescription());
        pluginCommand.setExecutor(plugin);
        pluginCommand.setTabCompleter(command.getTabCompleter());
        pluginCommand.setUsage(command.getUsage());
      } catch (InstantiationException | InvocationTargetException | IllegalAccessException
          | NoSuchMethodException | ClassNotFoundException e) {
        Logger.getLogger("EllyCommand").severe("Could not register command \"" + command.getName() + "\"");
      }
    }
  }

  private EllyCommand analyzeCommand(Command command, Method method) {
    List<String> aliases = Lists.newArrayList(command.aliases());
    CommandInstance instance = new CommandInstance(command.min(), command.max(),
        command.permissions(), getArguments(method), method);
    for (EllyCommand ellyCommand : commands) {
      if (Collections.getIntersection(aliases, ellyCommand.getAliases()).size() >= 1) {
        // Add any additional aliases to the command
        aliases.stream().filter(alias -> !ellyCommand.hasAlias(alias)).forEach(ellyCommand::addAlias);

        ellyCommand.addInstance(instance);

        return ellyCommand;
      }
    }

    // If no commands with matching aliases have been found, then make a new command
    EllyCommand ellyCommand = new EllyCommand(command);
    ellyCommand.addInstance(instance);
    return ellyCommand;
  }

  @SuppressWarnings("unchecked")
  private List<Argument> getArguments(Method method) {
    Class[] paramters = method.getParameterTypes();
    List<Argument> arguments = Lists.newArrayList();
    for (int i = 1; i < paramters.length; i++) {
      int[] multiArgs = Argument.getMultiArgs(method, i);
      arguments.add(new Argument(Argument.isRequired(method.getParameterAnnotations()[i]),
          multiArgs[0], multiArgs[1], registry.getProviderRegistry().getProvider(paramters[i])));
    }
    return arguments;
  }

  private List<Method> findCommandMethods(Class clazz) {
    List<Method> methods = Lists.newArrayList();
    for (Method method : clazz.getDeclaredMethods()) {
      Command command = method.getDeclaredAnnotation(Command.class);
      Class[] parameters = method.getParameterTypes();
      if (command != null
          && Modifier.isStatic(method.getModifiers())
          && method.getReturnType().equals(void.class)
          && parameters.length >= 1 && parameters[0].equals(CommandContext.class)) {
        methods.add(method);
      }
    }
    return methods;
  }

}
