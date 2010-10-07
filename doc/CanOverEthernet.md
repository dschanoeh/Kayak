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

Configuration frame
-------------------

### Set baudrate

Binary structure: 
001 00 [20 bit baudrate]

Information frame
-----------------

Alive frame
-----------