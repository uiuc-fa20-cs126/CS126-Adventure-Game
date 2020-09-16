package pfister.game;

/**
 * Abstract class specifying a RoomExit Navigation between rooms is described by RoomExit and its
 * implementing subclasses
 */
public abstract class RoomExit {

  /** The description written to the console when the player leaves the room */
  private final String description;

  /** The room this exit sends the player to */
  private final String nextRoom;

  protected RoomExit(String description, String nextRoom) {
    this.description = description;
    this.nextRoom = nextRoom;
  }

  public String getDescription() {
    return description;
  }

  public String getNextRoom() {
    return nextRoom;
  }
}
