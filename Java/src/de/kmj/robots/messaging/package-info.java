/**
 * Classes and interfaces for the communication between a RobotEngine
 * and the application which controls its behavior.
 * <p>
 * Regardless of the robot type, communication with their
 * {@link de.hcm.robots.RobotEngine} is based on the following protocol:
 * <ul>
 * <li>
 * The control application sends a
 * {@link de.hcm.robots.messaging.CommandMessage} to the RobotEngine.
 * </li>
 * <li>
 * The RobotEngine tries to execute the action specified by the CommandMessage
 * by converting it to instructions for this particular robot.
 * </li>
 * <li>
 * The RobotEngine sends at least one
 * {@link de.hcm.robots.messaging.StatusMessage} back to the control application
 * to inform it about the success, failure or detailed progress of the command
 * execution.
 * </li>
 * </ul>
 * <p>
 * Both MessageTypes can be converted to and from XML syntax as a human-readable
 * representation for various communication channels. For example, the
 * {@link de.hcm.robots.messaging.MessageServer} class in this package provides
 * a UDP connection for receiving commands via network.
 * <p>
 * The interfaces {@link de.hcm.robots.messaging.CommandMessageHandler} and
 * {@link de.hcm.robots.messaging.StatusMessageHandler} provide additional
 * flexibility with regards to the source of the commands and the receiver of
 * status updates.
 * <p>
 * The classes {@link de.hcm.robots.messaging.MessageServer} and
 * {@link de.hcm.robots.messaging.MessageClient} can be used to establish a UDP
 * connection between a RobotEngine (server) and an arbitrary control
 * application (client).
 */
package de.kmj.robots.messaging;
