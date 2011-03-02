Kayak
=====

Kayak is a application for CAN bus diagnosis and monitoring. Its main goals are a simple interface and platform independence.
Kayak is implemented in pure Java and has no platform specific dependencies. It includes a complete CAN bus abstraction model that can be included in other applications that need do handle CAN frames.
This is possible because [Socket CAN](http://developer.berlios.de/projects/socketcan/) and TCP/IP are used as an abstraction layer above the CAN controller hardware.
The [socketcand](https://github.com/dschanoeh/socketcand) provides the bridge between the Socket CAN device on a linux machine and the TCP/IP socket of Kayak.
To build Kayak follow the instructions in BUILD.md

### What is implemented yet:
* abstract bus, receiver, sender - model
* .kcd bus description format
* project and log file management
* simple raw view of CAN frames
 
### Why the name Kayak?
Because a kayak is a small, lightweight and versatile boat compared to a canoe. 
