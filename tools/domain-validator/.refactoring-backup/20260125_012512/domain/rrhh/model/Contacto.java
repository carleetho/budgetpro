package com.budgetpro.domain.rrhh.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object encapsulating contact information. Enforces validation for email
 * format.
 */
public final class Contacto {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private final String email;
    private final String telefono;
    private final String direccion;

    private Contacto(String email, String telefono, String direccion) {
        if (email != null && !email.isBlank()) {
            validateEmail(email);
        }
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
    }

    public static Contacto of(String email, String telefono, String direccion) {
        return new Contacto(email, telefono, direccion);
    }

    private void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Contacto contacto = (Contacto) o;
        return Objects.equals(email, contacto.email) && Objects.equals(telefono, contacto.telefono)
                && Objects.equals(direccion, contacto.direccion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, telefono, direccion);
    }

    @Override
    public String toString() {
        return "Contacto{" + "email='" + email + '\'' + ", telefono='" + telefono + '\'' + ", direccion='" + direccion
                + '\'' + '}';
    }
}
