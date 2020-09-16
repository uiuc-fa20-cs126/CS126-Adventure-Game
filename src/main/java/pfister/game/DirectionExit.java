package pfister.game;

/** This exit is used when the player types "Go 'direction'" */
public final class DirectionExit extends RoomExit {
  /** The direction the player must type to use this exit */
  private final Direction direction;
  /** The text displayed after the user enter the command, but before entering the next room */
  private final String outcomeText;

  public DirectionExit(
      String description, String nextRoom, Direction direction, String outcomeText) {
    super(description, nextRoom);
    this.direction = direction;
    this.outcomeText = outcomeText;
  }

  public String getOutcomeText() {
    return outcomeText;
  }

  public Direction getDirection() {
    return direction;
  }
}
