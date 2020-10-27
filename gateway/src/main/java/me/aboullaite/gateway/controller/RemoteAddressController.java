package me.aboullaite.gateway.controller;

import me.aboullaite.gateway.NettyReactiveWebServerConfig;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A demo controller that shows the customized behavior of {@link ServerHttpRequest#getRemoteAddress}. The
 * customization class is CustomizedHttpForwardedHeaderHandler in {@link NettyReactiveWebServerConfig}.
 */
@RestController
public class RemoteAddressController {

    /**
     * @param request
     * @return Value of X-Forwarded-For header, as well as result of {@link ServerHttpRequest#getRemoteAddress}
     */
    @GetMapping("/remote_address")
    public String getRemoteAddress(ServerHttpRequest request) {
        return String.format("X-Forwarded-For: %s\n" +
                        "getRemoteAddress() returns %s",
                request.getHeaders().get("X-Forwarded-For"),
                request.getRemoteAddress()
        );
    }
}
