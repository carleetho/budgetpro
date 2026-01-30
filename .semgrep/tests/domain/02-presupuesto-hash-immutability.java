package com.budgetpro.domain.finanzas.presupuesto.model;

import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityHashService;
import java.util.UUID;

public class Presupuesto {
    private String integrityHashApproval;

    public void tamper(String newHash) {
        // ruleid: 02-presupuesto-hash-immutability
        this.integrityHashApproval = newHash;
    }

    public void anotherTamper(Presupuesto other, String newHash) {
        // ruleid: 02-presupuesto-hash-immutability
        other.integrityHashApproval = newHash;
    }

    public void aprobar(UUID approvedBy, IntegrityHashService hashService) {
        // ok: 02-presupuesto-hash-immutability
        this.integrityHashApproval = hashService.calculateApprovalHash(this);
    }
}
