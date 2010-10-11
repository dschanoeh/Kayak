Introduction
============

The standard way to connect Kayak to a real (socket)CAN source is CanOverEthernet. This protocol
will be used to provide simple and fast access to the functionality of the CAN source.
UDP frames will be used for data transfer because they represent the way CAN frames are
transmitted. On a CAN bus there is also no flow control (except of the ACK slot) and the sender can 
not be sure that the data was received by the correct receiver.
Because UDP is not connection based it can be used even if the ethernet connection is very bad
(via WLAN) or cables are disconnected and connected again during in-car configuration.

Service discovery
=================
A server will bind to a new and random port for each bus he provides access to. Because of that
service discovery is necessary. The server sends a broadcast to port 42000 on the subnet where the
bus ports were bound. The interval for these discovery beacons shall not be longer than three
seconds. For each offered socket there must be one beacon with the following information:
* Description of the service
* Name of the bus (this should be the same as the device name)
* URL with port and IP address
* Device type the service is running on
  * socketCAN - general socketCAN service on a linux machine
  * embedded - embedded linux with access to a bus over socketCAN
  * adapter - e.g. microcontroller driven CAN to ethernet adapter

Connection establishment
========================
When a Kayak instance has found an existing bus via the service discovery it can to connect to the
device over TCP to configure it and to register for frame reception.
The configuration connection may be used to set a baudrate or apply filters for specific CAN IDs.
When the configuration was successful the client can start the frame transmission and may close the
TCP connection. The frame transfer happens via UDP and is initiated by the service after the client
has registered his UDP port and started the transmission.

Frame format
============
The information is encoded bitwise and there are many pieces of information with different sizes to
be transmitted. Every transmitted frame begins with a 3 bit type of frame (data, command,
information, alive). The following bits have a different meaning which is defined in the following
subsections for each frame type.
The formats are designed to always have a length that is divisible by 8 bit. Data is mostly aligned
to byte borders to allow easy handling without shifting especially for microcontroller applications.  

Frame types
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

Change the baudrate of the CAN controller to the given value.
001 000 000000 [20 bit baudrate]

### Get baudrate

Request the transmission of the baudrate value that is currently set.
001 010 00

### Get bus name

Every service must provide a human readable name for each bus it provides. If the service is a
socketCAN gateway it shall report the name that is used for the linux CAN device.

001 011 00

### Begin transmission

This command opens the connection for CAN frame transfer in both directions.

001 100 00

### End transmission

This command stops the transmission of CAN frames over the connection. The client also shall not
send any frames while the transmission is stopped.

001 101 00

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