package com.budgetpro.infrastructure.rest;
import org.springframework.web.bind.annotation.*;
@RestController
class MyController {
    @PostMapping("/test")
    public void create(@RequestBody 
    // ruleid: 03-dto-validation-boundary
    com.budgetpro.domain.model.Estimacion e) {}
}
