Ubermep
========
This project was developed during my bachelor-thesis: "Design and implementation of a peer-to-peer overlay network".


The implementation of ubermep supports:

* creation, connection and disconnection of peer-to-peer overlay networks
* sending messages of following message exchange patterns (mep):
* (unreliable / reliable) unicast
* (unreliable / reliable) multicast
* single request single response
* single request multi response
* multi request multi response


Installation
------------
No installation is required. To build ubermep, you need Java 6 and [Apache Maven][maven].

To build, run 'mvn install'.


Usage
-----
'java -jar ubermep-cmdline/target/ubermep-cmdline-0.1.one-jar.jar'


License
-------
This project is licenced under a BSD license. For details, see src/etc/license.txt

[maven]:http://maven.apache.org/
