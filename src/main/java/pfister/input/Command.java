package pfister.input;

/**
 * Holds what command a user entered
 */
public final class Command {

  /**
   * The command name the user entered
   */
  public String command;
  /**
   * The argument the user gave to the command
   */
  public String argument;

  public Command(String command) {
    this.command = command;
    this.argument = "";
  }
  public Command(String command, String argument) {
    this.command = command;
    this.argument = argument;
  }

  @Override
  public String toString() {
    return (command + " " + argument).trim();
  }
}
