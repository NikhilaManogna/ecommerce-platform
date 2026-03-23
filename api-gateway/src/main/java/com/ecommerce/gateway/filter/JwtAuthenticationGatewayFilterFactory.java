package com.ecommerce.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    private final SecretKey secretKey;

    public JwtAuthenticationGatewayFilterFactory(@Value("${security.jwt.secret}") String secret) {
        super(Config.class);
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            if (request.getMethod() == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }
            if (config.getOpenEndpoints().stream().anyMatch(path::startsWith)) {
                return chain.filter(exchange);
            }

            List<String> headers = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
            if (headers == null || headers.isEmpty() || !headers.get(0).startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            try {
                String token = headers.get(0).substring(7);
                Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
                return chain.filter(exchange);
            } catch (Exception ex) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    public static class Config {
        private List<String> openEndpoints = List.of("/api/users/register", "/api/users/login");

        public List<String> getOpenEndpoints() {
            return openEndpoints;
        }

        public void setOpenEndpoints(List<String> openEndpoints) {
            this.openEndpoints = openEndpoints;
        }
    }
}
