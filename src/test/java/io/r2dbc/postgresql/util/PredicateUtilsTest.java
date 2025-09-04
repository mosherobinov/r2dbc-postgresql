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

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Unit tests for {@link PredicateUtils}.
 */
class PredicateUtilsTest {

    @Test
    void notNullArgument() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PredicateUtils.not(null))
            .withMessage("t must not be null");
    }

    @Test
    void notWithTruePredicate() {
        Predicate<String> alwaysTrue = s -> true;
        Predicate<String> negated = PredicateUtils.not(alwaysTrue);
        
        assertThat(negated.test("test")).isFalse();
    }

    @Test
    void notWithFalsePredicate() {
        Predicate<String> alwaysFalse = s -> false;
        Predicate<String> negated = PredicateUtils.not(alwaysFalse);
        
        assertThat(negated.test("test")).isTrue();
    }

    @Test
    void notWithConditionalPredicate() {
        Predicate<String> isEmpty = String::isEmpty;
        Predicate<String> isNotEmpty = PredicateUtils.not(isEmpty);
        
        assertThat(isNotEmpty.test("")).isFalse();
        assertThat(isNotEmpty.test("test")).isTrue();
    }

    @Test
    void orNullArgument() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PredicateUtils.or((Predicate<String>[]) null))
            .withMessage("ts must not be null");
    }

    @Test
    void orEmptyPredicates() {
        assertThatIllegalStateException()
            .isThrownBy(() -> PredicateUtils.or())
            .withMessage("Unable to combine predicates together via logical OR");
    }

    @Test
    void orSinglePredicate() {
        Predicate<String> isEmpty = String::isEmpty;
        Predicate<String> result = PredicateUtils.or(isEmpty);
        
        assertThat(result.test("")).isTrue();
        assertThat(result.test("test")).isFalse();
    }

    @Test
    void orTwoPredicates() {
        Predicate<String> isEmpty = String::isEmpty;
        Predicate<String> isSingleChar = s -> s.length() == 1;
        Predicate<String> result = PredicateUtils.or(isEmpty, isSingleChar);
        
        assertThat(result.test("")).isTrue();      // empty
        assertThat(result.test("a")).isTrue();     // single char
        assertThat(result.test("test")).isFalse(); // neither empty nor single char
    }

    @Test
    void orMultiplePredicates() {
        Predicate<String> isEmpty = String::isEmpty;
        Predicate<String> isSingleChar = s -> s.length() == 1;
        Predicate<String> isTest = "test"::equals;
        Predicate<String> result = PredicateUtils.or(isEmpty, isSingleChar, isTest);
        
        assertThat(result.test("")).isTrue();      // empty
        assertThat(result.test("a")).isTrue();     // single char
        assertThat(result.test("test")).isTrue();  // equals "test"
        assertThat(result.test("hello")).isFalse(); // none of the conditions
    }

    @Test
    void orWithAllFalsePredicates() {
        Predicate<String> alwaysFalse1 = s -> false;
        Predicate<String> alwaysFalse2 = s -> false;
        Predicate<String> result = PredicateUtils.or(alwaysFalse1, alwaysFalse2);
        
        assertThat(result.test("test")).isFalse();
    }

    @Test
    void orWithAllTruePredicates() {
        Predicate<String> alwaysTrue1 = s -> true;
        Predicate<String> alwaysTrue2 = s -> true;
        Predicate<String> result = PredicateUtils.or(alwaysTrue1, alwaysTrue2);
        
        assertThat(result.test("test")).isTrue();
    }
}