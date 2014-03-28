<!--

    Copyright (C) 2011-2014 Barchart, Inc. <http://www.barchart.com/>

    All rights reserved. Licensed under the OSI BSD License.

    http://www.opensource.org/licenses/bsd-license.php

-->
## Netty Client/Server Components

Simple APIs for implementing network services with Netty 4.0. Designed to be simple, modular and easy to
create and destroy for unit testing and on-the-fly updates.

#### Modules

common - Core shared classes for client and server.

server - Network server implementations for TCP, UDP, UDT, SCTP, Websockets, HTTP.

rest/server - Higher-level REST server APIs for quickly building services on top of the HTTP server.

rest/client - REST client API for accessing rest/server services, with pluggable transports.

client - Streaming client toolkit for easily assembling a Netty data pipeline behind application-specific APIs.
