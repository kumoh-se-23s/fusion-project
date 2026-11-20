package auth;

import com.minihttp.core.route.dependency.Dependency;
import com.minihttp.packet.HttpRequest;
import exception.UnauthorizedException;
import util.JwtTokenProvider;

public class CurrentUser implements Dependency<Long> {
    private final JwtTokenProvider tokenProvider = new JwtTokenProvider();

    public Long resolve(HttpRequest httpRequest) {

        String jwt = httpRequest.header().get("Authorization")
                .orElseThrow(UnauthorizedException::new);
        return tokenProvider.getUserId(jwt);
    }
}
