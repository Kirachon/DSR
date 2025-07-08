import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("Generating BCrypt hashes for demo passwords:");
        System.out.println("admin123: " + encoder.encode("admin123"));
        System.out.println("staff123: " + encoder.encode("staff123"));
        System.out.println("citizen123: " + encoder.encode("citizen123"));
    }
}
