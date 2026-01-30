package com.budgetpro.tools.naming.rules;

import com.budgetpro.tools.naming.model.NamingViolation;
import com.budgetpro.tools.naming.model.ValidationRule;
import com.budgetpro.tools.naming.model.ViolationSeverity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Regla para servicios de dominio: Deben estar en el paquete '/service/'.
 */
public class DomainServiceRule implements ValidationRule {

    @Override
    public List<NamingViolation> validate(Path filePath, String className) {
        List<NamingViolation> violations = new ArrayList<>();

        String pathStr = filePath.toString().replace('\\', '/').toLowerCase();

        if (className.contains("Service") && !pathStr.contains("/service/")) {
            violations.add(new NamingViolation(filePath, className, className, ViolationSeverity.WARNING,
                    "NAMING: Servicio de dominio fuera del paquete /service/",
                    "Los servicios de dominio deben estar ubicados en un paquete que contenga '/service/'."));
        }

        return violations;
    }
}
