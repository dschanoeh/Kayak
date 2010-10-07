Introduction
============

The standard way to connect Kayak to a real (socket)CAN source is CanOverEthernet. This protocol
will be used to provide simple and fast access to the functionality of the CAN source.
UDP frames will be used for data transfer because they represent the way CAN frames are
transmitted. On a CAN bus there is also no flow control (except of the ACK slot) and the sender can 
not be sure that the data was received by the correct receiver.
Because UDP is not connection based it can be used even if the ethernet connection is very bad
(via WLAN) or cables are disconnected and connected again during in-car configuration.

Packet types
============

The Protocol separates data from configuration frames. Each Frame only contains one atomic 
information.

Data frame
----------

### Standard data frame

Binary structure:
000 0 [11 bit identifier] 000000 [3 bit length] [0-8 byte data]

### Extended data frame

Binary structure:
000 1 [29 bit identifier] 0000 [3 bit length] [0-8 byte data]

Command frame
-------------

A command frame is sent from the client to the host to generate an action. This can be a new
configuration for the CAN controller or a request to receive some information about the server.

### Set baudrate

Binary structure: 
001 000 000000 [20 bit baudrate]

### Get baudrate

Binary structure: 
001 010 000000 [20 bit baudrate]

### Get bus name

Binary structure: 
001 011 00

Information frame
-----------------

An information frame is sent from the server to the client after the client has requested the
transmission of this information.

### Current baudrate
010 010 000000 [20 bit baudrate]

### Bus name


Alive frame
-----------

Because there is no connection between server and client both sides can not be sure if the partner
is still active and listening. Both sides can send an alive request with a random id. The other
side must respond with the same id in a yet to be defined time. The id is used to guarantee that
request and response match.

### Request alive

011 0 [12 bit random id]

### Alive response

011 1 [12 bit id (same as request)]