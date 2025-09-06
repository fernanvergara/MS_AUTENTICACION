package co.com.sti.usecase.authentication.jwt;

public interface IJwtUtilsAuth {
    String generate( String email, Integer idRole );
    Boolean passwordMatch(String password, String hashedPassword);
}
