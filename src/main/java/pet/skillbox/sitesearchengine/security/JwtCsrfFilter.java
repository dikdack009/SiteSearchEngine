package pet.skillbox.sitesearchengine.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.micrometer.core.instrument.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.web.csrf.*;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import pet.skillbox.sitesearchengine.repositories.JwtTokenRepository;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtCsrfFilter extends OncePerRequestFilter {

    private final CsrfTokenRepository tokenRepository;

    private final HandlerExceptionResolver resolver;

    public JwtCsrfFilter(CsrfTokenRepository tokenRepository, HandlerExceptionResolver resolver) {
        this.tokenRepository = tokenRepository;
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        request.setAttribute(HttpServletResponse.class.getName(), response);
        CsrfToken csrfToken = this.tokenRepository.loadToken(request);
        boolean missingToken = csrfToken == null;
        if (missingToken) {
            csrfToken = this.tokenRepository.generateToken(request);
            tokenRepository.saveToken(csrfToken, request, response);
        }

        request.setAttribute(CsrfToken.class.getName(), csrfToken);
        request.setAttribute(csrfToken.getParameterName(), csrfToken);
        if (request.getServletPath().equals("/api/auth/login") || request.getServletPath().equals("/api/registration")) {
            System.out.println("Авторизация");
            try {
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                resolver.resolveException(request, response, null, new MissingCsrfTokenException(csrfToken.getToken()));
            }
        } else {
            String actualToken = request.getHeader(csrfToken.getHeaderName());
            System.out.println("actualToken " + actualToken);
            if (actualToken == null) {
                actualToken = request.getParameter(csrfToken.getParameterName());
            }
            System.out.println("actualToken " + actualToken);
            try {
                if (!StringUtils.isEmpty(actualToken)) {
                    Jws<Claims> k =  Jwts.parser()
                            .setSigningKey(((JwtTokenRepository) tokenRepository).getSecret())
                            .parseClaimsJws(actualToken);
                    System.out.println(k.toString());
                    filterChain.doFilter(request, response);
                } else
                    resolver.resolveException(request, response, null, new InvalidCsrfTokenException(csrfToken, actualToken));
            } catch (JwtException e) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Invalid CSRF token found for " + UrlUtils.buildFullRequestUrl(request));
                }

                if (missingToken) {
                    resolver.resolveException(request, response, null, new MissingCsrfTokenException(actualToken));
                } else {
                    resolver.resolveException(request, response, null, new InvalidCsrfTokenException(csrfToken, actualToken));
                }
            }
        }
    }
}
