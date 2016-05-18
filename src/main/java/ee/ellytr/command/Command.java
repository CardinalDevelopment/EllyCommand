package ee.ellytr.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

  String[] aliases();
  String description();
  int min() default 0;
  int max() default Integer.MAX_VALUE;
  String usage() default "";
  String[] permissions() default {};

}
