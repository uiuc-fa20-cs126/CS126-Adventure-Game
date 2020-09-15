package pfister.game;

/**
 * This exit is used when the player types "Go 'direction'"
 */
public class DirectionExit extends RoomExit {
  /**
   * The direction the player must type to use this exit
   */
  Direction direction;
  String outcomeText = "";
}
