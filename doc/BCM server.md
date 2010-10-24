BCM server
==========

The BCM server provides a network interface to the Socket CAN broadcast manager of the host. It can be controlled over a single TCP socket and supports transmission and reception of CAN frames. The used protocol is ASCII based and commands have the following structure:

### Command structure ###
< interface command ival_s ival_us can_id can_dlc [data]* >

Extensions
----------

If the BCM server is supposed to be a fundament for Kayak it needs to provide a few more features:

### Service discovery ###
Because configuration shall be as easy as possible and the virtual CAN bus and the Kayak instance are not neccessarily on the same machine a machanism for service discovery is neccessary.

The server sends a broadcast beacon to port 42000 on the subnet where the server port was bound. The interval for these discovery beacons shall not be longer than three seconds. Because the BCM server handles all communication (even for multiple busses) over a single TCP connection the broadcast must provide information about all busses that are accessible through the BCM server.

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
        <URL>socketCAN://127.0.0.1:28600</URL>
        <Bus name="vcan0"/>
        <Bus name="vcan1"/>
    </CANBeacon>


