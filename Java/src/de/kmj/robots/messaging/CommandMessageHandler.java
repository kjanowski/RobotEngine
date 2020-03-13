package de.kmj.robots.messaging;

/**
 * Interface for a class which receives commands for the {@link de.hcm.robots.RobotEngine}.
 * @author Kathrin Janowski
 */
public interface CommandMessageHandler {
    
    public void handleCommandMessage(CommandMessage message);
}
