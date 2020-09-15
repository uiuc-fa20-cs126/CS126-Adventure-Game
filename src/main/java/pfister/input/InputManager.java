package pfister.input;

import java.util.Scanner;

public class InputManager {
  private static Scanner scanner = new Scanner(System.in);

  public static Command promptForInput() {
    System.out.print("> ");
    String readLine = "";
    while (readLine.equals("")) {
     readLine = scanner.nextLine();
    }
    String[] split = readLine.split("\\s");
    String commandString = split[0].trim();
    String argumentString = "";
    if (split.length > 1) {
      split[0] = "";
      argumentString = String.join(" ",split).trim();
    }
    return new Command(commandString.toLowerCase(),argumentString.toLowerCase());
  }
}
