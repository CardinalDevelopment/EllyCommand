package ee.ellytr.command.util;

import com.google.common.collect.Lists;

import java.util.List;

public class Collections {

  public static <T> List<T> getIntersection(List<T> list1, List<T> list2) {
    List<T> intersection = Lists.newArrayList(list1);
    intersection.retainAll(list2);
    return intersection;
  }

}
