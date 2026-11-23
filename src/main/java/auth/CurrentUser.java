package auth;

import com.minihttp.core.route.dependency.Dependency;import com.minihttp.core.route.dependency.security.BearerSchema;
import com.minihttp.packet.HttpRequest;
import exception.UnauthorizedException;
import util.JwtTokenProvider;

public class CurrentUser extends BearerSchema<Long> {
    private final JwtTokenProvider tokenProvider = new JwtTokenProvider();

    @Override
    protected Long authenticate(String token) {
        if (!this.tokenProvider.validateToken(token)) {
            throw new UnauthorizedException();
        }

        return tokenProvider.getUserId(token);
    }
}
