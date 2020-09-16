package pfister.game;

import java.util.Optional;

/** Holds the cardinal directions */
public enum Direction {
  North,
  South,
  East,
  West;

  /**
   * Attempts to parse a string to a direction, ignoring case
   *
   * @param dir a cardinal direction
   * @return A optional direction, empty if the string could not be parsed to a direction
   */
  public static Optional<Direction> parseDirection(String dir) {
    String titleCased = dir.substring(0, 1).toUpperCase() + dir.substring(1).toLowerCase();
    try {
      return Optional.of(Direction.valueOf(titleCased));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }
}
