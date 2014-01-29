Kayak
=====

Kayak is a application for CAN bus diagnosis and monitoring. Its main goals are a simple interface and platform independence.
Kayak is implemented in pure Java and has no platform specific dependencies. It includes a complete CAN bus abstraction model that can be included in other applications that need do handle CAN frames.
This is possible because [Socket CAN](http://developer.berlios.de/projects/socketcan/) and TCP/IP are used as an abstraction layer above the CAN controller hardware.
The [socketcand](https://github.com/dschanoeh/socketcand) provides the bridge between the Socket CAN device on a linux machine and the TCP/IP socket of Kayak.
Existing .dbc files with CAN message specifications can be converted into the Kayak .kcd format using [CANBabel](https://github.com/julietkilo/CANBabel). Afterwards Kayak is able to decode CAN frames and to display and interpret the messages and signals.
To build Kayak follow the instructions in BUILD.md

### What is implemented yet:
* abstract bus, receiver, sender - model
* .kcd bus description format
* project and log file management
* simple raw view of CAN frames
* sending of single and repetitive CAN frames
* creation and replay of log files
 
### Why the name Kayak?
Because a kayak is a small, lightweight and versatile boat compared to a canoe. 
