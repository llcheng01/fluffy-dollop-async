package net.hereis.johnny.playasync;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Service
public class GitHubLookupService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubLookupService.class);

    private final RestTemplate restTemplate;

    public GitHubLookupService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = new RestTemplateBuilder(new ProxyCustomizer()).build();
    }

    @Async
    public CompletableFuture<User> findUser(String user) throws InterruptedException {
        logger.info("Looking up " + user);
        String url = String.format("https://api.github.com/users/%s", user);
        User results = restTemplate.getForObject(url, User.class);
        // Artifical delay of 1s for demonstration purposes
        Thread.sleep(1000L);
        // get does not block
        return CompletableFuture.completedFuture(results);
    }

    static class ProxyCustomizer implements RestTemplateCustomizer {
        @Override
        public void customize(RestTemplate restTemplate) {
            HttpHost proxy = new HttpHost("proxy.inbcu.com", 80);

            HttpClient httpClient = HttpClientBuilder.create()
                    .setRoutePlanner(new DefaultProxyRoutePlanner(proxy) {

                        @Override
                        public HttpRoute determineRoute(HttpHost host, HttpRequest request, HttpContext context) throws HttpException {
                            return super.determineRoute(host, request, context);
                        }
                    }).build();

            restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
        }
    }
}
