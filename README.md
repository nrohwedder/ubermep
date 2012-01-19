Ubermep
========
This project was developed during my bachelor-thesis: _Design and implementation of a peer-to-peer overlay network_.


The implementation of ubermep supports:

* creation of a peer-to-peer overlay network
* connection and disconnection to a network
* sending messages of following message exchange patterns (mep):
  * (unreliable / reliable) unicast
  * (unreliable / reliable) multicast
  * single request single response
  * single request multi response
  * multi request multi response


Installation
------------
No installation is required. To build ubermep, you need Java 6 and [Apache Maven][maven].

To build, run `mvn install`.

Usage
-----
Please see: [Examples]


License
-------
This project is licenced under a BSD license. For details, see [license]

[maven]:http://maven.apache.org/
[Examples]:ubermep/wiki/Examples
[license]:https://github.com/nrohwedder/ubermep/blob/master/src/etc/license.txt