package util;

import com.minihttp.core.handler.Handler;
import com.minihttp.core.middleware.Middleware;
import com.minihttp.packet.HttpRequest;
import com.minihttp.packet.HttpResponse;
import exception.NotFoundException;
import exception.UnauthorizedException;

import java.util.Optional;

public class JwtAuthenticationMiddleware implements Middleware {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationMiddleware(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public HttpResponse handle(HttpRequest request, Handler next) throws Exception {
        Optional<String> authHeader = request.header().get("Authorization");
        String[] parts = authHeader.orElseThrow(NotFoundException::new).split(" ");

        String jwtToken = parts[1];

        if (jwtTokenProvider.validateToken(jwtToken)) {
            return next.handle(request);
        }

        throw new UnauthorizedException();
    }
}
