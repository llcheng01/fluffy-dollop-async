package net.hereis.johnny.playasync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class PlayAsyncAppRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(PlayAsyncAppRunner.class);

    private GitHubLookupService gitHubLookupService;

    @Autowired
    public PlayAsyncAppRunner(GitHubLookupService gitHubLookupService) {
        this.gitHubLookupService = gitHubLookupService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Start the clock
        long start = System.currentTimeMillis();

        // Kick of multiple, asynchronous lookups
        CompletableFuture<User> blog1 = gitHubLookupService.findUser("PivotalSoftware");
        CompletableFuture<User> blog2 = gitHubLookupService.findUser("CloudFoundry");
        CompletableFuture<User> blog3 = gitHubLookupService.findUser("Spring-Project");

        // Wait until they are all done
        CompletableFuture.allOf(blog1, blog3, blog2);

        // Print results, including elapsed time
        logger.info("Elapsed time: " + (System.currentTimeMillis() - start));
        logger.info("--> " + blog3.get());
        logger.info("--> " + blog2.get());
        logger.info("--> " + blog1.get());
    }
}
