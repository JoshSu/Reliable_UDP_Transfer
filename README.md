# Reliable_UDP_Transfer
make UDP to TCP/IP


The first program is a sender. The sender runs on one host and reads the names of large data files as
input. The sender reads the data files, breaks the file contents into UDP datagrams and broadcasts
the packets to multiple receivers running on the same subnet. The sender must be able to hold on
to previously transmitted datagrams for retransmission as needed. Files get distributed one at a
time (i.e the sender works on transmitting the contents of one file before starting to transmit the
next file). We deliberately make communication between sender and receiver unreliable to simulate
poor-quality links, highly overloaded links or congested routers in a large-scale private network.
The sender has to randomly drop a specified percentage of acknowledgment packets it receives to
simulate an unreliable environment.

The receiver program fakes unreliable communication due to poor quality links, overloaded links
or congested routers in a large-scale private network by deliberately dropping some percentage of
the incoming datagrams, thus acting as if the datagrams never arrived. A command-line argument
specifies the percentage of packets lost. To simulate unreliable communication, the receiver has
to randomly drop a specified percentage of incoming packets. All receivers will communicate with
the sender on the same port. The sender and receivers will use an Automatic Repeat reQuest
protocol of your own devising. The receiver sends ACK or NACK packets to the sender, and the
sender responds to the ACK or NACK packets by sending more packets to the receiver(s). You
have to design an efficient ACK/NACK protocol between the sender and receiver that minimizes
ACK/NACK packet overhead and data packet retransmission. The receiver has to reconstruct the
received data files on the remote hosts. The receivers should construct only complete files when
possible.


The single sender is responsible for building packets and transmitting the packets to one or more
receivers. The unreliability of the communication fabric used to carry packets from sender to
receiver(s) means that the sender has to hold on to packets until all the receivers acknowledge
receipt of individual packets. The sender must also know who its receivers are and which receivers
have received individual packets. Thus, the sender must have the ability to receive some kind
of acknowledgment packet from the receiver(s) that either acknowledge which packets have been
received (a positive acknowledgment or ACK), or request packets that have not been received (a
negative acknowledgment or NACK). The sender retransmits packets to receivers that have not
received the requested packets. The sender can only discard a packet (and any associated reception
status information) only when it knows that all receivers have actually received the packet.
Packets sent from the sender may not arrive at a receiver at all or they may arrive in a different
order from the order they were sent. The possibility of packet retransmission means that a receiver
might even get the same packet more than once. It is therefore necessary to carry some kind of
packet sequencing information as metainformation communicated from sender to receiver. The
receiver is responsible for receiving the packets, extracting application data from the packets in the
correct order, requesting missing packets and ultimately building the proper output files.
