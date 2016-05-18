package ee.ellytr.command.util;

public class Strings {

  public static String getTechnicalName(String in) {
    return in.toUpperCase().replaceAll(" ", "_");
  }

}
