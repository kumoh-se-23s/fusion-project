package util;

import com.minihttp.core.handler.Handler;
import com.minihttp.core.middleware.Middleware;
import com.minihttp.packet.HttpRequest;
import com.minihttp.packet.HttpResponse;
import exception.UnauthorizedException;

public class JwtAuthenticationMiddleware implements Middleware {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationMiddleware(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public HttpResponse handle(HttpRequest request, Handler next) throws Exception {
        String jwtToken = extractToken(request);

        if (jwtTokenProvider.validateToken(jwtToken)) {
            return next.handle(request);
        }

        throw new UnauthorizedException();
    }

    private String extractToken(HttpRequest request) {
        String value = request.header().get("Authorization")
                .orElseThrow(UnauthorizedException::new);

        if (!value.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException();
        }

        String token = value.substring(BEARER_PREFIX.length()).trim();

        if (token.isEmpty()) {
            throw new UnauthorizedException();
        }

        return token;
    }
}
