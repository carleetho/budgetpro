package com.budgetpro.performance;

import java.util.List;

public class NPlusOneTest {

    public void testViolation(List<Long> ids, MyRepository repository) {
        // ruleid: budgetpro.performance.n-plus-one-query
        for (Long id : ids) {
            repository.findById(id);
        }

        // ruleid: budgetpro.performance.n-plus-one-query
        ids.forEach(id -> {
            repository.save(new Entity(id));
        });

        // ruleid: budgetpro.performance.n-plus-one-query
        for (int i = 0; i < ids.size(); i++) {
            repository.updateDetails(ids.get(i));
        }
    }

    public void testOk(List<Long> ids, MyRepository repository) {
        // ok: budgetpro.performance.n-plus-one-query
        repository.findAllById(ids);

        // ok: budgetpro.performance.n-plus-one-query
        for (Long id : ids) {
            System.out.println(id);
        }

        // ok: budgetpro.performance.n-plus-one-query
        // Calling a non-repository/service method is fine
        for (Long id : ids) {
            helperMethod(id);
        }
    }

    private void helperMethod(Long id) {
    }

    interface MyRepository {
        void findById(Long id);

        void save(Entity e);

        void updateDetails(Long id);

        void findAllById(List<Long> ids);
    }

    class Entity {
        Entity(Long id) {
        }
    }
}
