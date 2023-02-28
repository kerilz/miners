/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under one or more contributor license agreements.
 * Licensed under a proprietary license. See the License.txt file for more information.
 * You may not use this file except in compliance with the proprietary license.
 */
package kyrylo;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public enum VariableType {
    STRING("String"),
    SHORT("Short"),
    LONG("Long"),
    DOUBLE("Double"),
    INTEGER("Integer"),
    BOOLEAN("Boolean"),
    DATE("Date"),
    OBJECT("Object"),
    JSON("Json");

    private static final Set<VariableType> NUMERIC_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            INTEGER, SHORT, LONG, DOUBLE
    )));
    private static final Map<String, VariableType> BY_LOWER_CASE_ID_MAP = Stream.of(VariableType.values())
            .collect(toMap(type -> type.getId().toLowerCase(), type -> type));

    private final String id;

    VariableType(final String id) {
        this.id = id;
    }

    @JsonValue
    public String getId() {
        return id;
    }

    public static VariableType getTypeForId(String id) {
        return Optional.ofNullable(id).map(String::toLowerCase).map(BY_LOWER_CASE_ID_MAP::get).orElse(null);
    }

    public static Set<VariableType> getNumericTypes() {
        return NUMERIC_TYPES;
    }
}