package org.room13.terminal.logging
{
	import flash.events.*;
	import flash.net.Socket;
	
	import mx.logging.AbstractTarget;
	import mx.logging.ILogger;
	import mx.logging.LogEvent;
	import mx.logging.LogEventLevel;

	public class SocketLoggingTarget extends AbstractTarget
	{
		private static const _CLASSNAME : String = "org.room13.terminal.logging.SocketLoggingTarget";
		private static const _COMMAND_CONNECT : uint = 1;
		private static const _COMMAND_RECORD : uint = 2;
		private static const _COMMAND_DISCONNECT : uint = 3;
		private var _name : String;
		private var _connected : Boolean = false;
		private var _accessCode : String;
		private var _socket : Socket;
		private var _logServer : String;
		private var _logServerPort : int;
		
		/**
		 * Creates instance of target with given name.
		 * @param name name of the application
		 * @param level maximum log level
		 * @param accessCode access code to remote server
		 * @param logServer log server name or its ip address
		 * @param logServerPort log server port
		 */		
		public function SocketLoggingTarget(name:String, accessCode:String = "", logServer:String = "localhost", logServerPort:uint = 3344, level:int = LogEventLevel.ALL)
		{
			super();
			_name = name;			
			this.level = level;
			_accessCode = accessCode;
			_logServer = logServer;
			_logServerPort = logServerPort;
			_socket = new Socket();
            configureListeners(_socket);
			connectToServer();
		}
		
	    override public function logEvent(event:LogEvent) : void {
	    	if (!_connected)
	    		return;
	    	if (!_socket.connected)
	    		return;
			
			_socket.writeByte(_COMMAND_CONNECT);
			_socket.writeUTF(_name);			
			_socket.writeUTF(_accessCode);

	    	//var stanza : String = "\nBEGIN:"+_recordCounter+":"+ILogger(event.target).category+":"+event.level+":"+new Date().getTime()+"\n"+event.message+"\nEND:"+_recordCounter+"\n";
			_socket.writeByte(_COMMAND_RECORD);
			_socket.writeUTF(ILogger(event.target).category);			
			_socket.writeInt(event.level);
			_socket.writeDouble(new Date().getTime());
			_socket.writeUTF(event.message);
    	}
    	
    	private function connectToServer() : void {
            _socket.connect(_logServer, _logServerPort);
    		_connected = true;
    	}

        private function configureListeners(dispatcher:IEventDispatcher):void {
            dispatcher.addEventListener(Event.CLOSE, closeHandler);
            dispatcher.addEventListener(Event.CONNECT, connectHandler);
            dispatcher.addEventListener(DataEvent.DATA, dataHandler);
            dispatcher.addEventListener(IOErrorEvent.IO_ERROR, ioErrorHandler);
            dispatcher.addEventListener(ProgressEvent.PROGRESS, progressHandler);
            dispatcher.addEventListener(SecurityErrorEvent.SECURITY_ERROR, securityErrorHandler);
        }

        private function closeHandler(event:Event):void {
            trace(_CLASSNAME+"["+_name+"]#closeHandler: " + event);
        	_connected = false;
        }

        private function securityErrorHandler(event:SecurityErrorEvent):void {
            trace(_CLASSNAME+"["+_name+"]#securityErrorHandler: " + event);
			_connected = false;
        }

        private function ioErrorHandler(event:IOErrorEvent):void {
            trace(_CLASSNAME+"["+_name+"]#ioErrorHandler: " + event);
            _connected = false;
        }

        private function connectHandler(event:Event):void {
            trace(_CLASSNAME+"["+_name+"]#connectHandler: " + event);
        }
		
		private function dataHandler(event:DataEvent):void {
            trace(_CLASSNAME+"["+_name+"]#dataHandler: " + event);
        }

        private function progressHandler(event:ProgressEvent):void {
            trace(_CLASSNAME+"["+_name+"]#progressHandler loaded:" + event.bytesLoaded + " total: " + event.bytesTotal);
        }
	}
}