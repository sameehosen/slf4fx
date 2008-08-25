package org.room13.slf4fx.messages;

import org.apache.mina.core.buffer.IoBuffer;
import org.room13.slf4fx.Message;
import static org.room13.slf4fx.Message.MessageTypeId.NewRecord;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * TODO: Document the class
 */
public class LogRecordMessage implements Message {
    public enum Level {
        ERROR, WARN, INFO, DEBUG, TRACE
    }

    private final CharsetEncoder _charsetEncoder = Charset.forName("UTF-8").newEncoder();
    private final CharsetDecoder _charsetDecoder = Charset.forName("UTF-8").newDecoder();
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

    public MessageTypeId getMessageId() {
        return NewRecord;
    }

    public IoBuffer toIoBuffer() throws CharacterCodingException {
        final IoBuffer buffer = IoBuffer.allocate(1);
        buffer.setAutoExpand(true);
        buffer.put((byte) getMessageId().ordinal());
        buffer.putPrefixedString(getCategory(), _charsetEncoder);
        buffer.putInt(getLevel().ordinal());
        buffer.putPrefixedString(getMessage(), _charsetEncoder);
        buffer.flip();
        return buffer;
    }

    public Message read(final IoBuffer in) throws CharacterCodingException {
        setCategory(in.getPrefixedString(_charsetDecoder).intern());
        final int levelIndex = in.getInt();
        setLevel(levelIndex < Level.values().length ? Level.values()[levelIndex] : Level.INFO);
        setMessage(in.getPrefixedString(_charsetDecoder).intern());
        return this;
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
