package auth;

import com.minihttp.core.route.dependency.Dependency;
import com.minihttp.packet.HttpRequest;
import util.JwtTokenProvider;

public class CurrentUser implements Dependency<Long> {
    private final JwtTokenProvider tokenProvider = new JwtTokenProvider();
    private final
    public Long resolve(HttpRequest httpRequest) {
        String jwt = httpRequest.header().get("Authorization")
                .orElseThrow(RuntimeException::new);

        String userToken = tokenProvider.decoding(jwt);

        // http.get("http://localhost/user/decode", jwt)
        return null;
    }
}
