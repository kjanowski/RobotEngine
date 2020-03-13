/**
 * \mainpage RobotEngine
 * 
 * The RobotEngine framework aims to provide a uniform interface
 * for robots of different models and by different manufacturers.
 * Its core idea is to separate high-level application logic from the code
 * which accesses a specific robot or virtual character.
 * This way, both the control application and the controlled agent(s)
 * can be exchanged in a modular way with minimal coding.
 * <p>
 * There are only two message types:
 * <ul>
 * <li>commands from the control application, sent to the agent's RobotEngine</li>
 * <li>status updates from the agent's RobotEngine, sent to the control application</li>
 * </ul>
 * Both are represented as human-readable, XML-based text in order to
 * facilitate debugging and maintenance.
 * The commands themselves describe single actions in the available modalities,
 * such as playing an animation file, turning the head to face a certain position
 * or speaking a sentence.
 * <p>
 * The purpose of a RobotEngine is NOT
 * to provide multimodal synchronization, autonomous behaviors
 * or intelligent reasoning, but to support the re-use of software which does.
 * <p>
 * For example, a dialog application would choose an appropriate text fragment,
 * plan the gestures to play during certain words, and maybe run a parallel
 * process for generating life-like gaze patterns.
 * It would then translate these behaviors to individual speech commands
 * with bookmarks inserted at the synchronization points,
 * as well as suitable collections of animation commands.
 * Then it will transmit each of these primitive commands to a given RobotEngine
 * and receive status updates in return, which it can use to determine
 * the proper timing for the dependent commands.
 * <p>
 * This way the high-level AI can easily be transferred to a different agent
 * without the need to re-implement the low-level API calls and regardless of
 * the software required to run the agent. Likewise, once there is a RobotEngine
 * for a specific agent, it can be connected to any compatible control application,
 * be it an autonomous dialog system, a graphical remote-control interface
 * or a quick test application.
 */

/**
 * Provides a common framework for robots of different models and by different
 * manufacturers.
 * <p>
 * The base package contains the abstract {@link de.hcm.robots.RobotEngine}
 * which must be implemented for each specific robot type.
 * <p>
 * Every specific RobotEngine serves to provide a uniform interface between the
 * robot's functionality and the controlling application, hiding the
 * robot-specific implementation details to facilitate the re-use of
 * robot-independent code. For example, a Wizard-of-Oz control interface can be
 * used for robots from different manufacturers simply by referencing a
 * different RobotEngine.
 * <p>
 * The other class in this package is a generic helper application which
 * provides a UDP server for connecting an arbitrary RobotEngine to an external
 * control application. It also accepts commands via console input.
 * <p>
 * A generic control application and useful GUI classes can be found in
 * sub-package {@link de.hcm.robots.controlApp}.
 * <p>
 * The message protocol for communicating with the RobotEngine is contained in
 * sub-package {@link de.hcm.robots.messaging}.
 *
 * @author Kathrin Janowski
 * @version 2.3.0
 */
package de.kmj.robots;
