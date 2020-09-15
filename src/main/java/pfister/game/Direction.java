package pfister.game;

import com.sun.javaws.exceptions.InvalidArgumentException;
import java.util.Optional;

public enum Direction {
  North,
  South,
  East,
  West;

  public static Optional<Direction> parseDirection(String dir) {
    String titleCased = dir.substring(0,1).toUpperCase() + dir.substring(1);
    try {
      return Optional.of(Direction.valueOf(titleCased));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}