package com.hoops.auth.application.port.out;

import com.hoops.auth.domain.vo.AuthProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AuthProvider에 따라 적절한 OAuthPort를 반환하는 Factory
 */
@Component
public class OAuthPortFactory {

    private final Map<AuthProvider, OAuthPort> oauthPorts;

    public OAuthPortFactory(List<OAuthPort> oauthPortList) {
        this.oauthPorts = oauthPortList.stream()
                .collect(Collectors.toMap(
                        OAuthPort::getProvider,
                        Function.identity(),
                        (existing, replacement) -> replacement  // 나중에 등록된 빈 우선 (@Primary Mock)
                ));
    }

    public OAuthPort getPort(AuthProvider provider) {
        OAuthPort port = oauthPorts.get(provider);
        if (port == null) {
            throw new UnsupportedOperationException(
                    "OAuth provider not supported: " + provider);
        }
        return port;
    }

    public boolean supports(AuthProvider provider) {
        return oauthPorts.containsKey(provider);
    }
}
