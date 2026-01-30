public class ValidationTest {
    // ruleid: 03-missing-input-validation
    @PostMapping("/test")
    public ResponseEntity<?> create(@RequestBody MyDto dto) {
        return ResponseEntity.ok().build();
    }

    // ruleid: 03-missing-input-validation
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody MyDto dto) {
        return ResponseEntity.ok().build();
    }
}

public class ValidationNegativeTest {
    // ok: 03-missing-input-validation
    @PostMapping("/test")
    public ResponseEntity<?> create(@Valid @RequestBody MyDto dto) {
        return ResponseEntity.ok().build();
    }
}
