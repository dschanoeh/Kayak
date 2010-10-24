BCM server
==========

The BCM server provides a network interface to the Socket CAN broadcast manager of the host. It can be controlled over a single TCP socket and supports transmission and reception of CAN frames. The used protocol is ASCII based and commands have the following structure:

### Command structure ###
    < interface command ival_s ival_us can_id can_dlc [data]* >

### Commands for transmission ###

##### Add a new frame for transmission #####
This command adds a new frame to the BCM queue. An intervall can be configured to have the frame sent cyclic.

Examples:
Send the CAN frame 123#1122334455667788 every second on vcan1
    < vcan1 A 1 0 123 8 11 22 33 44 55 66 77 88 >

Send the CAN frame 123#1122334455667788 every 10 usecs on vcan1
    < vcan1 A 0 10 123 8 11 22 33 44 55 66 77 88 >

Send the CAN frame 123#42424242 every 20 msecs on vcan1
    < vcan1 A 0 20000 123 4 42 42 42 42 >

##### Update a frame #####
This command updates a frame transmission job that was created via the 'A' command with new content. The transmission timers are not touched

Examle:
Update the CAN frame 123#42424242 with 123#112233 - no change of timers
    < vcan1 U 0 0 123 3 11 22 33 >

##### Delete a send job #####
A send job can be removed with the 'D' command.

Example:
Delete the cyclic send job from above
    < vcan1 D 0 0 123 0 >

##### Send a single frame #####
This command is used to send a single CAN frame only once.

Example:
Send a single CAN frame without cyclic transmission
    < can0 S 0 0 123 0 >

Extensions
----------

If the BCM server is supposed to be a fundament for Kayak it needs to provide a few more features:

### Service discovery ###
Because configuration shall be as easy as possible and the virtual CAN bus and the Kayak instance are not neccessarily on the same machine a machanism for service discovery is neccessary.

The server sends a broadcast beacon to port 42000 on the subnet where the server port was bound. The interval for these discovery beacons shall not be longer than three seconds. Because the BCM server handles all communication (even for multiple busses) over a single TCP connection the broadcast must provide information about all busses that are accessible through the BCM server.

##### Content #####

* Description of the service
* Name of the busses (in case of socketCAN and embedded this should be the same as the device name)
* URL with port and IP address
* Device type the service is running on

##### Device types ######

* SocketCAN - general socketCAN service on a linux machine
* embedded - embedded linux with access to a bus over socketCAN
* adapter - e.g. microcontroller driven CAN to ethernet adapter

##### Structure #####

For simple parsing and a human readable schema XML is used to structure the information in a CAN beacon.

##### Example #####

    <CANBeacon>
        <Device name="HeartOfGold" type="SocketCAN"/>
        <URL>can://127.0.0.1:28600</URL>
        <Bus name="vcan0"/>
        <Bus name="vcan1"/>
    </CANBeacon>


