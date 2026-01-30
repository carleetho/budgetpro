public class SecretsTest {
    // ruleid: 01-hardcoded-secrets
    String pass = "password=myhardcodedpass";

    // ruleid: 01-hardcoded-secrets
    String apiKey = "RESEND_API_KEY=re_123456789";

    // ruleid: 01-hardcoded-secrets
    String postgres = "POSTGRES_PASSWORD=admin";
}

public class SecretsNegativeTest {
    // ok: 01-hardcoded-secrets
    @Value("${db.password}")
    private String dbPassword;

    // ok: 01-hardcoded-secrets
    String normalString = "This is a regular string without secrets";
}
