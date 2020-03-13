# RobotEngine

A generic messaging framework for connecting arbitrary virtual or robotic agents to arbitrary control applications.


# Basic Design Principles

The core idea is that the control application and the agent exchange two statndardized types of messages, which are independent of any particular agent's or control application's API. One message type is the 'Command Message' which describes an individual, uni-modal action that the agent should execute. The other type is the 'Status Message' which informs the control application of the action's execution progress.

An agent-specific 'Robot Engine' receives the Command Message, translates the generic description to the necessary API calls and monitors their execution in order to send back, at the very least, a Status Message to report the action's success or failure. The connected 'Control Application' is responsible for everything else - from input processing to multi-modal action synchronization, automated behaviors and higher-level reasoning.

This separation makes it easy to re-use the application logic with a robot from a different manufacturer or with a completely virtual agent.


# Current Implementation Status

* The *Java* implementation is the primary, most advanced version. This one also includes a generic control application that connects to an external RobotEngine application via UDP.
* The *Python* implementation contains the core classes for parsing and handling the two message types.
* The *C#* implementation contains classes for the two message types, but
