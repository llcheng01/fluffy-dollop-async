package net.hereis.johnny.playasync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    public @ResponseBody Future<List<User>> get(@RequestParam List<String> input) throws Exception {
        return concatenateAsync(input);
    }

    private Future<List<User>> concatenateAsync(List<String> input) throws Exception{
        // Start the clock
        long start = System.currentTimeMillis();

        ExecutorService yourOwnExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        // Create the collection of futures
        List<CompletableFuture<User>> futures = new ArrayList<>(input.size());
        List<User> responses = new ArrayList<>();

        for (String name : input) {

            CompletableFuture<User> requestFuture = gitHubLookupService.findUser(name)
                    .exceptionally(ex -> {
                System.out.println(ex);
                return null;
            });
            futures.add(requestFuture);
        }

        // Completed output
//        CompletableFuture<String> output = new CompletableFuture<>();

        // Kick off multiple, asynchronous lookups
//        CompletableFuture<User> blog1 = gitHubLookupService.findUser("PivotalSoftware");
//        CompletableFuture<User> blog2 = gitHubLookupService.findUser("CloudFoundry");
//        CompletableFuture<User> blog3 = gitHubLookupService.findUser("Spring-Project");

        // Wait until they are all done
        // CompletableFuture<Void> jobsDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));

        // Print results, including elapsed time
        logger.info("Elapsed time: " + (System.currentTimeMillis() - start));
//        logger.info("--> " + blog3.get());
//        logger.info("--> " + blog2.get());
//        logger.info("--> " + blog1.get());

        return sequence(futures);
    }

    private static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allDoneFuture =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allDoneFuture.thenApply(v ->
                futures.stream().
                        map(future -> future.join()).
                        collect(Collectors.<T>toList())
        );
    }
}