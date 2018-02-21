package net.hereis.johnny.playasync;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Controller
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private GitHubLookupService gitHubLookupService;

    @RequestMapping(value = "/home")
    public String home() {
        return "home";
    }
    // Exercise using curl http://localhost:8080/async?input=lorem,ipsum,dolor,sit,amet
    @RequestMapping(path = "async", method = RequestMethod.GET)
    public Future<String> get(@RequestParam List<String> input) throws Exception {
        return null;
    }

    private Future<String> concatenateAsync(List<String> input) throws Exception{
        // Create the collection of futures
        List<Future<User>> futures = input.stream()
                .map(str -> supplyAsync(() ->
                {
                    try {
                        gitHubLookupService.findUser(str);
                    } catch (InterruptedException ie) {
                        throw new RuntimeException(ie);
                    }
                }))
                .collect(Collectors.toList());


        // Start the clock
        long start = System.currentTimeMillis();

        // Completed output
        CompletableFuture<String> output = new CompletableFuture<>();

        // Kick off multiple, asynchronous lookups
        CompletableFuture<User> blog1 = gitHubLookupService.findUser("PivotalSoftware");
        CompletableFuture<User> blog2 = gitHubLookupService.findUser("CloudFoundry");
        CompletableFuture<User> blog3 = gitHubLookupService.findUser("Spring-Project");

        // Wait until they are all done
        CompletableFuture<Void> jobsDone = CompletableFuture.allOf(blog1, blog3, blog2);

        // Print results, including elapsed time
        logger.info("Elapsed time: " + (System.currentTimeMillis() - start));
//        logger.info("--> " + blog3.get());
//        logger.info("--> " + blog2.get());
//        logger.info("--> " + blog1.get());

        // Once all the futures have completed, build out the result string from get results.
        jobsDone.thenAccept(ignored -> {
           StringBuilder sb = new StringBuilder();
            input.forEach(i -> {
                try {
                    sb.append(gitHubLookupService.findUser(i).get());
                } catch (Exception e) {
                    output.completeExceptionally(e);
                }
            });
            output.complete(sb.toString());
        });

        return output;
    }
}
