# Start server #
To start server with default configuration open the terminal (run cmd.exe on Windows), change current directory to directory where slf4fx binary package installed and execute following command:
```
java -jar slf4fx-server-deps.jar
```
Instead of that command you can use scripts `slf4fx.cmd` (on windows) and `slf4fx.sh` (on `*`nix). Those scripts included in binary archive.

You will see next text
```
11:00:07.508 INFO  SLF4FxServer : default local address: localhost/127.0.0.1:18888
11:00:07.514 INFO  SLF4FxServer : session timeout: 60 seconds
11:00:07.514 INFO  SLF4FxServer : reader buffer size: 1024 bytes
11:00:07.515 INFO  SLF4FxServer : known credentials: []
11:00:07.515 INFO  SLF4FxServer : <policy-file-request/> response: <?xml version='1.0'?>
<!DOCTYPE cross-domain-policy SYSTEM 'http://www.adobe.com/xml/dtds/cross-domain-policy.dtd'>
<cross-domain-policy>
    <allow-access-from domain='*' to-ports='18888'/>
</cross-domain-policy>
11:00:07.649 INFO  SLF4FxServer : slf4fx server started in 145 ms
```

# Stop server #
To stop server just press `Control-C` in server window (terminal window with running server).

## Configure server ##
Configuration is also easy - just provide additional options in server command line. The command `java -jar slf4fx-server-deps.jar --help`prints list of available options
```
usage: java -jar slf4fx-server.jar [OPTIONS]
SLF4Fx simple server
 -b,--bind <ADDRESS[:PORT]>        bind SLF4Fx server to this address
 -d,--disable-policy               disable any socket policy for Adobe Flash
                                   Player
 -h,--help                         print this message
 -k,--known-applications <FILE>    known applications descriptor file(one pair
                                   APPLICATION=SECRET per line)
 -p,--policy-file <FILE>           socket policy file for Adobe Flash Player
 -r,--reader-buffer-size <BYTES>   protocol decoder buffer size
 -t,--session-timeout <TIMEOUT>    session timeout in seconds
```

## Configure logging ##
The simple server integrated with log4j logging framework. A simple but enough log4j configuration included in binary package so it could be used out-of-box. All log events will be written to console. Additionaly all records with `slf4fx.*` category will be saved to`slf4fx-client.log` file in current directory so they could be analyzed later.
You can provide you own log4j configuration. For that cop file `misc/log4j.properties.sample` to directory where you start slf4fx server as file `log4j.properties`. Edit that file and start server. See Log4J documentation for further details.

## Understanding logging category ##
All log events from applications will have category in form:
```
slf4fx.APPLICATION_ID.LOG_RECORD_CATEGORY
```
Where **slf4fx** is constant suffix. **APPLICATION\_ID** is application name given as parameter to _Slf4FxLoggingTarget_ on client side. Application id is optional parameter. **LOG\_RECORD\_CATEGORY** is logger category on client side.