Kayak
=====

Kayak aims to be a Java framework and GUI for CAN data handling. It will include an
abstract data model for different CAN hard- and software solutions.
[SocketCAN](http://developer.berlios.de/projects/socketcan/) and IP will be used as an abstraction layer above the hardware.
Platform independence is a goal and therefore all logic will be implemented in Java. To run the framework on a Windows client
a socketCAN over ethernet protocol will be implemented.
 
### What is implemented yet:
* abstract bus, receiver, sender - model
* CAN logfile replay
* logfile writing
* basic .dbc file import

### What is on the list to be implemented:
* Saving and loading from an own format
* Receiving and sending of CAN frames (via ethernet, socketCAN or other modules)
* Parsing of frames and generation of data objects
* A GUI to watch and control busses
 
### Why the name Kayak?
Because a kayak is a small, lightweight and versatile boat compared to a canoe. 