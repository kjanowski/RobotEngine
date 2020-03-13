package de.kmj.robots.messaging;

/**
 * Interface for a class which listens to status updates from a {@link de.hcm.robots.RobotEngine}.
 * @author Kathrin Janowski
 */
public interface StatusMessageHandler {
    public void handleStatusMessage(StatusMessage message);
}
