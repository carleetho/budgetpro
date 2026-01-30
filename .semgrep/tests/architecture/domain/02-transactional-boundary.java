package com.budgetpro.domain.service;
import org.springframework.transaction.annotation.Transactional;
// ruleid: 02-transactional-boundary
@Transactional
class DomainService {
    // ruleid: 02-transactional-boundary
    @Transactional
    public void action() {}
}
