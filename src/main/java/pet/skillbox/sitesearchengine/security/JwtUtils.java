package pet.skillbox.sitesearchengine.security;

import io.jsonwebtoken.Claims;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import pet.skillbox.sitesearchengine.model.Role;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JwtUtils {

    public static JwtAuthentication generate(Claims claims) {
        final JwtAuthentication jwtInfoToken = new JwtAuthentication();
        jwtInfoToken.setRoles(getRoles(claims));
        jwtInfoToken.setFirstName(claims.get("firstName", String.class));
        jwtInfoToken.setUsername(claims.getSubject());
        return jwtInfoToken;
    }

    private static Set<Role> getRoles(Claims claims) {
        final String roles = claims.get("roles", String.class);
        return Set.of(Role.valueOf(roles));
    }
    public static String getUsername(Claims claims) {
        return claims.getSubject();
    }

}