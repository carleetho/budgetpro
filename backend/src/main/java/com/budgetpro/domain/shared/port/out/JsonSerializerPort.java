package com.budgetpro.domain.shared.port.out;

/**
 * Port for JSON serialization, decoupling domain from specific libraries like
 * Jackson.
 */
public interface JsonSerializerPort {
    String toJson(Object object);

    <T> T fromJson(String json, Class<T> clazz);
}
