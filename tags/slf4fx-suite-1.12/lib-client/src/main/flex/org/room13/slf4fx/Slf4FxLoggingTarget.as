/*
 * Copyright (C) 2009 Dmitry Motylev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE.txt-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.room13.slf4fx
{
    import flash.events.*;
    import flash.net.Socket;

    import mx.logging.AbstractTarget;
    import mx.logging.ILogger;
    import mx.logging.LogEvent;
    import mx.logging.LogEventLevel;

    /**
     * Connects to slf4fx server and sent all log events to it.
     * When disconnected can hold up to 1000 log event until connected.
     * Following is mapping from Flex logging event levels to slf4j logging record levels:
     * <ul>
     * <li>DEBUG to DEBUG</li>
     * <li>INFO to INFO</li>
     * <li>WARN to WARN</li>
     * <li>ERROR to ERROR</li>
     * <li>FATAL to ERROR</li>
     * </ul>
     */
    public class Slf4FxLoggingTarget extends AbstractTarget
    {
        private static const _CLASSNAME : String = "org.room13.slf4fx.Slf4FxLoggingTarget";

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

        private static const _MAX_PENDING_MESSAGES : int = 1000;
        private var _pendingLogEvents : Array = new Array();

        /**
         * Creates instance of target for application with given name.
         *
         * @param applicationName name of the application
         * @param secret that secret and application name will be used as credentials during connection to slf4fx server.
         * The secret is "" by default
         * @param logServer log server name or its ip address. Default value is "localhost"
         * @param logServerPort log server port. Default value is 18888
         */
        public function Slf4FxLoggingTarget(applicationName:String, secret:String = "",
                                            logServer:String = "localhost", logServerPort:uint = 18888)
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
                addPendingLogEvent(event);
                doConnect();
                return;
            }
            if (_currentState != _STATE_IDLE) {
                addPendingLogEvent(event);
                return;
            }
            writeLogEvent(event);
        }

        private function writeLogEvent(event:LogEvent) : void {
            _socket.writeByte(_MSG_NEW_RECORD);
            _socket.writeUTF(ILogger(event.target).category);
            _socket.writeInt(convertToSlf4jLevel(event.level));
            _socket.writeUTF(event.message);
            _socket.flush();
        }

        private function addPendingLogEvent(event:LogEvent) : void {
            if (_pendingLogEvents.length == _MAX_PENDING_MESSAGES)
                return;
            _pendingLogEvents.push(event);
        }

        private function convertToSlf4jLevel(level:int):int {
            /* Next is enum from corresponding LogRecordMessage.java
             public enum Level {
             ERROR, WARN, INFO, DEBUG, TRACE
             }
             */
            switch (level) {
                case LogEventLevel.FATAL:
                case LogEventLevel.ERROR:
                    return 0;
                case LogEventLevel.WARN:
                    return 1;
                case LogEventLevel.INFO:
                    return 2;
                case LogEventLevel.DEBUG:
                    return 3;
                default:
                    return 2;
            }
        }

        private function doConnect() : void {
            _socket.connect(_host, _port);
            _currentState = _STATE_WAITING_CONNECTION;
        }

        private function doAccessRequest() : void {
            _socket.writeByte(_MSG_ACCESS_REQUEST);
            _socket.writeUTF(_applicationName);
            _socket.writeUTF(_secret);
            _socket.flush();
            _currentState = _STATE_WAITING_ACCESS_RESPONSE;
        }

        private function configureListeners(dispatcher:IEventDispatcher):void {
            dispatcher.addEventListener(Event.CLOSE, closeHandler);
            dispatcher.addEventListener(Event.CONNECT, connectHandler);
            dispatcher.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
            dispatcher.addEventListener(ProgressEvent.SOCKET_DATA, dataHandler);
            dispatcher.addEventListener(SecurityErrorEvent.SECURITY_ERROR, securityErrorHandler);
        }

        private function closeHandler(event:Event):void {
            cleanUp(false);
        }

        private function securityErrorHandler(event:SecurityErrorEvent):void {
            cleanUp();
        }

        private function ioErrorHandler(event:IOErrorEvent):void {
            cleanUp();
        }

        private function cleanUp(doSocketClose:Boolean = true):void {
            if (doSocketClose) {
                try {
                    _socket.close();
                } catch(e:Error) {
                    // ignore any error here
                }
            }
            _currentState = _STATE_DISCONNECTED;
            _pendingLogEvents.splice(0, _pendingLogEvents.length);
        }

        private function connectHandler(event:Event):void {
            if (_currentState != _STATE_WAITING_CONNECTION) {
                return;
            }
            doAccessRequest();
        }

        private function dataHandler(event:ProgressEvent):void {
            if (_currentState != _STATE_WAITING_ACCESS_RESPONSE) {
                cleanUp();
                return;
            }
            if (_socket.readByte() != _MSG_ACCESS_RESPONSE) {
                cleanUp();
                return;
            }
            if (_socket.readByte() == 1) {
                _currentState = _STATE_IDLE;
                for each (var logEvent:LogEvent in _pendingLogEvents) {
                    writeLogEvent(logEvent);
                }
                _pendingLogEvents.splice(0, _pendingLogEvents.length);
                return;
            }
            cleanUp();
        }
    }
}
