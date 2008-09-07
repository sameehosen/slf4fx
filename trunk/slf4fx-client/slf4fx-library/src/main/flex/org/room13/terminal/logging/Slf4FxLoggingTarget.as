package org.room13.terminal.logging
{
import flash.events.*;
import flash.net.Socket;

import mx.logging.AbstractTarget;
import mx.logging.ILogger;
import mx.logging.LogEvent;
import mx.logging.LogEventLevel;


public class Slf4FxLoggingTarget extends AbstractTarget
{
    private static const _CLASSNAME : String = "org.room13.terminal.logging.Slf4FxLoggingTarget";

    private static const _MSG_UNKNOWN : uint = 0;
    private static const _MSG_ACCESS_REQUEST : uint = 1;
    private static const _MSG_ACCESS_RESPONSE : uint = 2;
    private static const _MSG_NEW_RECORD : uint = 3;
    private static const _MSG_CLOSE : uint = 4;

    private static const _STATE_DISCONNECTED : uint = 0;
    private static const _STATE_WAITING_CONNECTION : uint = 1;
    private static const _STATE_WAITING_ACCESS_RESPONSE : uint = 2;
    private static const _STATE_IDLE : uint = 3;

    private var _applicationName : String;
    private var _secret : String;
    private var _host : String;
    private var _port : int;
    private var _socket : Socket;

    private var _currentState : uint = _STATE_DISCONNECTED;

    /**
     * Creates instance of target with given name.
     * name name of the application
     * secret access code to remote server
     * @param logServer log server name or its ip address
     * @param logServerPort log server port
     */
    public function Slf4FxLoggingTarget(applicationName:String, secret:String = "", logServer:String = "localhost", logServerPort:uint = 8888)
    {
        super();
        this.level = LogEventLevel.ALL;
        _applicationName = applicationName;
        _secret = secret;
        _host = logServer;
        _port = logServerPort;
        _socket = new Socket();
        configureListeners(_socket);
    }

    override public function logEvent(event:LogEvent) : void {
        if (_currentState == _STATE_DISCONNECTED) {
            doConnect();
            return;
        }
        if (_currentState != _STATE_IDLE) {
            return;
        }
        _socket.writeByte(_MSG_NEW_RECORD);
        _socket.writeUTF(ILogger(event.target).category);
        _socket.writeInt(event.level);
        _socket.writeUTF(event.message);
    }

    private function doConnect() : void {
        _socket.connect(_host, _port);
        _currentState = _STATE_WAITING_CONNECTION;
    }

    private function doAccessRequest() : void {
        _socket.writeByte(_MSG_ACCESS_REQUEST);
        _socket.writeUTF(_applicationName);
        _socket.writeUTF(_secret);
        _currentState = _STATE_WAITING_ACCESS_RESPONSE;
    }

    private function configureListeners(dispatcher:IEventDispatcher):void {
        dispatcher.addEventListener(Event.CLOSE, closeHandler);
        dispatcher.addEventListener(Event.CONNECT, connectHandler);
        dispatcher.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
        dispatcher.addEventListener(ProgressEvent.SOCKET_DATA, dataHandler);
        dispatcher.addEventListener(SecurityErrorEvent.SECURITY_ERROR, securityErrorHandler);
    }

    //noinspection JSUnusedLocalSymbols
    private function closeHandler(event:Event):void {
        _currentState = _STATE_DISCONNECTED;
    }

    //noinspection JSUnusedLocalSymbols
    private function securityErrorHandler(event:SecurityErrorEvent):void {
        _socket.close();
        _currentState = _STATE_DISCONNECTED;
    }

    //noinspection JSUnusedLocalSymbols
    private function ioErrorHandler(event:IOErrorEvent):void {
        _socket.close();
        _currentState = _STATE_DISCONNECTED;
    }

    //noinspection JSUnusedLocalSymbols
    private function connectHandler(event:Event):void {
        if (_currentState != _STATE_WAITING_CONNECTION) {
            return;
        }
        doAccessRequest();
    }

    //noinspection JSUnusedLocalSymbols
    private function dataHandler(event:ProgressEvent):void {
        if (_currentState != _STATE_WAITING_ACCESS_RESPONSE) {
            return;
        }
        if (_socket.readByte() != _MSG_ACCESS_RESPONSE) {
            return;
        }
        if (_socket.readByte() == 1) {
            _currentState = _STATE_IDLE;
            return;
        }
        _socket.close();
    }
}
}