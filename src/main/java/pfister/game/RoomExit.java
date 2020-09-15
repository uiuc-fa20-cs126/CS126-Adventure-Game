package pfister.game;

/**
 * Abstract class specifying a RoomExit
 * Navigation between rooms is described by RoomExit and its implementing subclasses
 */
public abstract class RoomExit {

  /**
   * The description written to the console when the player leaves the room
   */
  private String description;
  /**
   * The room this exit sends the player to
   */
  private String nextRoom;

  public String getDescription() {
    return description;
  }

  public String getNextRoom() {
    return nextRoom;
  }
}
