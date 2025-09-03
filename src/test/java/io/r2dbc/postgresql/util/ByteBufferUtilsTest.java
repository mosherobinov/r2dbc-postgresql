/*
 * Copyright 2017-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.r2dbc.postgresql.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Unit tests for {@link ByteBufferUtils}.
 */
class ByteBufferUtilsTest {

    @Test
    void decodeNullByteBuffer() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> ByteBufferUtils.decode(null))
            .withMessage("byteBuffer must not be null");
    }

    @Test
    void decodeEmptyByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(0);
        String result = ByteBufferUtils.decode(buffer);
        
        assertThat(result).isEmpty();
    }

    @Test
    void decodeAsciiString() {
        String original = "Hello World";
        ByteBuffer buffer = StandardCharsets.UTF_8.encode(original);
        String result = ByteBufferUtils.decode(buffer);
        
        assertThat(result).isEqualTo(original);
    }

    @Test
    void decodeUtf8String() {
        String original = "Hello 世界 🌍";
        ByteBuffer buffer = StandardCharsets.UTF_8.encode(original);
        String result = ByteBufferUtils.decode(buffer);
        
        assertThat(result).isEqualTo(original);
    }

    @Test
    void encodeNullCharSequence() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> ByteBufferUtils.encode(null))
            .withMessage("s must not be null");
    }

    @Test
    void encodeEmptyString() {
        ByteBuffer result = ByteBufferUtils.encode("");
        
        assertThat(result.remaining()).isZero();
        assertThat(result.position()).isEqualTo(result.limit());
    }

    @Test
    void encodeAsciiString() {
        String original = "Hello World";
        ByteBuffer result = ByteBufferUtils.encode(original);
        
        // Reset position to read the buffer
        result.flip();
        String decoded = StandardCharsets.UTF_8.decode(result).toString();
        assertThat(decoded).isEqualTo(original);
    }

    @Test
    void encodeUtf8String() {
        String original = "Hello 世界 🌍";
        ByteBuffer result = ByteBufferUtils.encode(original);
        
        // Reset position to read the buffer
        result.flip();
        String decoded = StandardCharsets.UTF_8.decode(result).toString();
        assertThat(decoded).isEqualTo(original);
    }

    @Test
    void encodeStringBuilder() {
        StringBuilder sb = new StringBuilder("Hello World");
        ByteBuffer result = ByteBufferUtils.encode(sb);
        
        // Reset position to read the buffer
        result.flip();
        String decoded = StandardCharsets.UTF_8.decode(result).toString();
        assertThat(decoded).isEqualTo("Hello World");
    }

    @Test
    void toByteBufferNullSource() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> ByteBufferUtils.toByteBuffer(null))
            .withMessage("source must not be null");
    }

    @Test
    void toByteBufferEmptyByteBuf() {
        ByteBuf source = Unpooled.EMPTY_BUFFER;
        ByteBuffer result = ByteBufferUtils.toByteBuffer(source);
        
        assertThat(result.remaining()).isZero();
    }

    @Test
    void toByteBufferWithData() {
        byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);
        ByteBuf source = Unpooled.wrappedBuffer(data);
        ByteBuffer result = ByteBufferUtils.toByteBuffer(source);
        
        assertThat(result.remaining()).isEqualTo(data.length);
        
        byte[] resultBytes = new byte[result.remaining()];
        result.get(resultBytes);
        assertThat(resultBytes).isEqualTo(data);
    }

    @Test
    void toByteBufferIsReadyToRead() {
        byte[] data = "Hello World".getBytes(StandardCharsets.UTF_8);
        ByteBuf source = Unpooled.wrappedBuffer(data);
        ByteBuffer result = ByteBufferUtils.toByteBuffer(source);
        
        // Buffer should be ready to read (position = 0, limit = data length)
        assertThat(result.position()).isZero();
        assertThat(result.limit()).isEqualTo(data.length);
        assertThat(result.remaining()).isEqualTo(data.length);
    }

    @Test
    void roundTripEncodeDecodeUtf8() {
        String original = "Hello 世界 🌍 with special chars: áéíóú ñ ü";
        ByteBuffer encoded = ByteBufferUtils.encode(original);
        encoded.flip(); // Reset position for reading
        String decoded = ByteBufferUtils.decode(encoded);
        
        assertThat(decoded).isEqualTo(original);
    }
}