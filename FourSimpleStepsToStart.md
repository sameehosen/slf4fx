## STEP1:  Add slf4fx to you Flex3 application ##
We will write our sample application that generates some log records on button click.
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
Don't forget to compile application :D

## STEP2: Start provided simple SLF4Fx server ##
```
java -jar slf4fx-server-deps.jar
```

## STEP3: Start your application ##
In our case we start sample application in browser.

![http://slf4fx.googlecode.com/svn/wiki/images/sample-app.png](http://slf4fx.googlecode.com/svn/wiki/images/sample-app.png)

## STEP4: Watch the log ##
```
01:06:27.883 DEBUG MyApplication : level=2 **DEBUG**
01:06:27.890 INFO  MyApplication : level=4 **INFO**
01:06:27.890 WARN  MyApplication : level=6 **WARN**
01:06:27.891 ERROR MyApplication : level=8 **ERROR**
01:06:27.891 ERROR MyApplication : level=1000 **FATAL**
```