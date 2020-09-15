package pfister.game;

import com.google.gson.annotations.SerializedName;

public class SmackExit extends RoomExit {
  @SerializedName("item")
  String itemUsed;
}
