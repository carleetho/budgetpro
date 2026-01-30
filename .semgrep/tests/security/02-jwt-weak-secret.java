public class JwtTest {
    // ruleid: 02-jwt-weak-secret
    String jwtSecret = "too-short";

    // ruleid: 02-jwt-weak-secret
    @Value("${jwt.secret}")
    String secret = "abc123xyz";
}

public class JwtNegativeTest {
    // ok: 02-jwt-weak-secret
    String jwtSecret = "this-is-a-very-long-secret-that-is-at-least-32-chars-long";

    // ok: 02-jwt-weak-secret
    @Value("${jwt.secret}")
    String secret = "AnotherVeryLongSecretForTestingPurposes123!";
}
