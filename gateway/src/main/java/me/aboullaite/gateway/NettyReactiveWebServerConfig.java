package me.aboullaite.gateway;

import io.netty.handler.codec.http.HttpRequest;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.netty.http.server.ConnectionInfo;
import reactor.netty.tcp.InetSocketAddressUtil;

import java.net.InetSocketAddress;
import java.util.function.BiFunction;

/**
 * This configuration customizes the behavior of remote address resolver. As a result,
 * {@link ServerHttpRequest#getRemoteAddress} will return IP address that is extracted from X-Forwarded-For header
 * by our {@link CustomizedHttpForwardedHeaderHandler}.
 */
@Configuration
public class NettyReactiveWebServerConfig implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

    @Override
    public void customize(NettyReactiveWebServerFactory factory) {
        factory.addServerCustomizers(httpServer -> httpServer.forwarded(new CustomizedHttpForwardedHeaderHandler()));
    }

    static class CustomizedHttpForwardedHeaderHandler
            implements BiFunction<ConnectionInfo, HttpRequest, ConnectionInfo> {

        static final String X_FORWARDED_IP_HEADER = "X-Forwarded-For";

        /**
         * Attempt to extract the last IP address from X-Forwarded-For header. If the header is present, create a
         * ConnectionInfo instance with the extracted IP address and then return it. Otherwise, return the
         * connectionInfo passed in. On the contrary, DefaultHttpForwardedHeaderHandler extracts the first IP
         * address from the header when the header is present.
         *
         * @param connectionInfo
         * @param request
         * @return
         */
        @Override
        public ConnectionInfo apply(ConnectionInfo connectionInfo, HttpRequest request) {
            String ipHeader = request.headers().get(X_FORWARDED_IP_HEADER);

            if (ipHeader != null) {
                String[] ips = ipHeader.split(",");
                String ip = ips[ips.length - 1].trim();

                // If the IP address is internal, we will skip it and extract the previous one.
                if (ips.length >= 2 && isInternal(ip)) {
                    ip = ips[ips.length - 2].trim();
                }

                InetSocketAddress remoteAddress = InetSocketAddressUtil.parseAddress(ip,
                        connectionInfo.getRemoteAddress().getPort());
                connectionInfo = connectionInfo.withRemoteAddress(remoteAddress);
            }

            return connectionInfo;
        }

        private boolean isInternal(String ip) {
            return ip.startsWith("10.");
        }
    }
}
