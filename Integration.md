# Client #
It's pretty easy to integrate slf4fx in your application. Just add slf3fx\_client.swc as library to you application and add target `Slf4FxLoggingTarget` to your log. Look at sample application.
```
<?xml version="1.0" encoding="utf-8"?>
<mx:Application xmlns:mx="http://www.adobe.com/2006/mxml" layout="absolute" applicationComplete="init()">
    <mx:Script><![CDATA[
        import mx.logging.LogEventLevel;
        import org.room13.slf4fx.Slf4FxLoggingTarget;
        import mx.logging.Log;
        import mx.logging.ILogger;

        private var _log : ILogger = Log.getLogger("org.room13.slf4fx.MyApplication");

        private function init() : void {
            Log.addTarget(new Slf4FxLoggingTarget("myApplication", "mySecret"));
        }

        private function generateLogRecords() : void {
            _log.debug("level={0} {1}", LogEventLevel.DEBUG, "**DEBUG**");
            _log.info("level={0} {1}", LogEventLevel.INFO, "**INFO**");
            _log.warn("level={0} {1}", LogEventLevel.WARN, "**WARN**");
            _log.error("level={0} {1}", LogEventLevel.ERROR, "**ERROR**");
            _log.fatal("level={0} {1}", LogEventLevel.FATAL, "**FATAL**");
        }
        ]]></mx:Script>
    <mx:Button label="Generate log records" x="20" y="20" click="generateLogRecords()"/>
</mx:Application>
```

# Server #
Binary package includes simple slf4fx server, that could be used for development. However you can integrate slf4fx server bean in your own application. Following code snippet from simple slf4fx server demonstrates how to integrate bean in application
```
    public static void main(String[] args) {
        
        // ...  
        
        try {
            final SLF4FxServer server = new SLF4FxServer();

            // ... doing additional configuration of server

            // add shutdown hook so server will be stoped on jvm shutdown event

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    server.stop();
                }
            }));

            server.start();
        } catch (Exception e) {
            System.err.println("failed to start slf4fx server");
            e.printStackTrace(System.err);
        }
    }
```
When you integrates slf4fx in your web backend you have to provide flex policy file, otherwise Flash Player will reject socket connections. SLF4FxServer bean includes default policy that allows socket connections from any domain to port 18888. This policy could be changed or disabled at all with setting bean properties.

# Logging category #
All log events from applications will have category in form:
```
slf4fx.APPLICATION_ID.LOG_RECORD_CATEGORY
```
Where **slf4fx** is constant suffix. **APPLICATION\_ID** is application name given as parameter to _Slf4FxLoggingTarget_ on client side. Application id is optional parameter. **LOG\_RECORD\_CATEGORY** is logger category on client side.