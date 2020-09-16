package pfister.game;

import com.google.gson.annotations.SerializedName;

/** This exit is used when the user types the "smack" command */
public final class SmackExit extends RoomExit {

  /** The item used in the smacking */
  @SerializedName("item")
  private final String itemUsed;

  public SmackExit(String description, String nextRoom, String itemUsed) {
    super(description, nextRoom);
    this.itemUsed = itemUsed;
  }

  public String getItemUsed() {
    return itemUsed;
  }
}
