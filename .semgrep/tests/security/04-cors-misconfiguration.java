public class CorsTest {
    public void configure(CorsRegistry registry) {
        // ruleid: 04-cors-misconfiguration
        registry.addMapping("/**").allowedOriginPatterns("*");

        // ruleid: 04-cors-misconfiguration
        registry.addMapping("/api/**").allowedOrigins("*");
    }
}

public class CorsNegativeTest {
    public void configure(CorsRegistry registry) {
        // ok: 04-cors-misconfiguration
        registry.addMapping("/**").allowedOriginPatterns("http://localhost:3000");
    }
}
