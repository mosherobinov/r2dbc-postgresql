/*
 * Copyright 2019-2020 the original author or authors.
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

package io.r2dbc.postgresql;

import io.r2dbc.postgresql.extension.Extension;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Extensions}.
 */
class ExtensionsTest {

    @Test
    void fromEmptyCollection() {
        Extensions extensions = Extensions.from(Collections.emptyList());
        assertThat(extensions.size()).isZero();
    }

    @Test
    void fromSingleExtension() {
        TestExtension extension = new TestExtension();
        Extensions extensions = Extensions.from(Collections.singletonList(extension));
        
        assertThat(extensions.size()).isEqualTo(1);
    }

    @Test
    void fromMultipleExtensions() {
        TestExtension extension1 = new TestExtension();
        TestExtension extension2 = new TestExtension();
        Extensions extensions = Extensions.from(Arrays.asList(extension1, extension2));
        
        assertThat(extensions.size()).isEqualTo(2);
    }

    @Test
    void autodetect() {
        Extensions extensions = Extensions.autodetect();
        // Should contain at least the test extension defined in META-INF/services
        assertThat(extensions.size()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void forEachWithMatchingType() {
        TestExtension extension1 = new TestExtension();
        AnotherTestExtension extension2 = new AnotherTestExtension();
        Extensions extensions = Extensions.from(Arrays.asList(extension1, extension2));
        
        AtomicInteger count = new AtomicInteger(0);
        extensions.forEach(TestExtension.class, e -> count.incrementAndGet());
        
        assertThat(count.get()).isEqualTo(1);
    }

    @Test
    void forEachWithNoMatchingType() {
        TestExtension extension = new TestExtension();
        Extensions extensions = Extensions.from(Collections.singletonList(extension));
        
        AtomicInteger count = new AtomicInteger(0);
        extensions.forEach(AnotherTestExtension.class, e -> count.incrementAndGet());
        
        assertThat(count.get()).isZero();
    }

    @Test
    void mergeWith() {
        TestExtension extension1 = new TestExtension();
        AnotherTestExtension extension2 = new AnotherTestExtension();
        
        Extensions extensions1 = Extensions.from(Collections.singletonList(extension1));
        Extensions extensions2 = Extensions.from(Collections.singletonList(extension2));
        
        Extensions merged = extensions1.mergeWith(extensions2);
        
        assertThat(merged.size()).isEqualTo(2);
    }

    @Test
    void mergeWithEmpty() {
        TestExtension extension = new TestExtension();
        Extensions extensions1 = Extensions.from(Collections.singletonList(extension));
        Extensions extensions2 = Extensions.from(Collections.emptyList());
        
        Extensions merged = extensions1.mergeWith(extensions2);
        
        assertThat(merged.size()).isEqualTo(1);
    }

    static class TestExtension implements Extension {
    }

    static class AnotherTestExtension implements Extension {
    }
}