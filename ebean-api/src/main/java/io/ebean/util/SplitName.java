package io.ebean.util;

/**
 * Helper for dot notation property paths.
 */
public final class SplitName {

  private static final char PERIOD = '.';

  /**
   * Add the two name sections together in dot notation.
   */
  public static String add(String prefix, String name) {
    if (prefix != null) {
      return prefix + "." + name;
    } else {
      return name;
    }
  }

  /**
   * Return the number of occurrences of char in name.
   */
  public static int count(String name) {

    int count = 0;
    for (int i = 0; i < name.length(); i++) {
      if (PERIOD == name.charAt(i)) {
        count++;
      }
    }
    return count;
  }

  /**
   * Return the parent part of the path.
   */
  public static String parent(String name) {
    if (name == null) {
      return null;
    } else {
      String[] s = split(name, true);
      return s[0];
    }
  }

  /**
   * Return the name split by last.
   */
  public static String[] split(String name) {
    return split(name, true);
  }

  /**
   * Return the first part of the name.
   */
  public static String begin(String name) {
    return splitBegin(name)[0];
  }

  public static String[] splitBegin(String name) {
    return split(name, false);
  }

  private static String[] split(String name, boolean last) {

    int pos = last ? name.lastIndexOf('.') : name.indexOf('.');
    if (pos == -1) {
      if (last) {
        return new String[]{null, name};
      } else {
        return new String[]{name, null};
      }
    } else {
      String s0 = name.substring(0, pos);
      String s1 = name.substring(pos + 1);
      return new String[]{s0, s1};
    }
  }

}
