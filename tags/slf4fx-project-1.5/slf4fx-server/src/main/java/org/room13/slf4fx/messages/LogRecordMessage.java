/*
 * Copyright 2008 Dmitry Motylev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.room13.slf4fx.messages;

import org.apache.mina.core.buffer.IoBuffer;
import org.room13.slf4fx.Message;
import static org.room13.slf4fx.Message.MessageType.NewRecord;

import java.nio.charset.CharacterCodingException;

/**
 * TODO: Document the class
 */
public class LogRecordMessage extends Message {
    public enum Level {
        ERROR, WARN, INFO, DEBUG, TRACE
    }

    private String _category;
    private Level _level;
    private String _message;

    public String getCategory() {
        return _category;
    }

    public void setCategory(final String category) {
        _category = category;
    }

    public Level getLevel() {
        return _level;
    }

    public void setLevel(final Level level) {
        _level = level;
    }

    public String getMessage() {
        return _message;
    }

    public void setMessage(final String message) {
        _message = message;
    }

    public MessageType getType() {
        return NewRecord;
    }

    public IoBuffer toIoBuffer() throws CharacterCodingException {
        final IoBuffer buffer = IoBuffer.allocate(1);
        buffer.setAutoExpand(true);
        buffer.put(getType().getValue());
        buffer.putPrefixedString(getCategory(), getCharsetEncoder());
        buffer.putInt(getLevel().ordinal());
        buffer.putPrefixedString(getMessage(), getCharsetEncoder());
        buffer.flip();
        return buffer;
    }

    protected void readIoBuffer(final IoBuffer in) throws CharacterCodingException {
        setCategory(in.getPrefixedString(getCharsetDecoder()).intern());
        final int levelIndex = in.getInt();
        setLevel(levelIndex < Level.values().length ? Level.values()[levelIndex] : Level.INFO);
        setMessage(in.getPrefixedString(getCharsetDecoder()).intern());
    }

    @SuppressWarnings({"RedundantIfStatement"})
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final LogRecordMessage that = (LogRecordMessage) o;

        if (_category != null ? !_category.equals(that._category) : that._category != null) return false;
        if (_level != that._level) return false;
        if (_message != null ? !_message.equals(that._message) : that._message != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = _category != null ? _category.hashCode() : 0;
        result = 31 * result + (_level != null ? _level.hashCode() : 0);
        result = 31 * result + (_message != null ? _message.hashCode() : 0);
        return result;
    }
}
