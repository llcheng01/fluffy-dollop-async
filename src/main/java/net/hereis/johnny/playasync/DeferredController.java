package net.hereis.johnny.playasync;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class DeferredController {

    @RequestMapping(path = "deferred", method = RequestMethod.GET)
    public @ResponseBody Future<String> get(@RequestParam List<String> input) {
//        CompletableFuture<String> future = new CompletableFuture<>();
//        return CompletableFuture.supplyAsync(() -> "in the background");
        
        return concatenateAsync(input);
    }

    private Future<String> concatenateAsync(List<String> input) {
        // Create the collection of futures.
        List<Future<String>> futures = input.stream()
                .map(str -> CompletableFuture.supplyAsync(() -> callApi(str)))
                .collect(Collectors.toList());

        // Restructure as varargs because that's what CompletableFuture.allOf requires.
        CompletableFuture<?>[] futuresAsVarArgs = futures.toArray(new CompletableFuture[futures.size()]);

        // Create a new future that completes once all of the previous futures complete.
        CompletableFuture<Void> jobsDone = CompletableFuture.allOf(futuresAsVarArgs);

        CompletableFuture<String> output = new CompletableFuture<>();

        // Once all of the futures have completed
        // build out the result string from future results
        jobsDone.thenAccept(ignored -> {
            StringBuilder sb = new StringBuilder();
            futures.forEach(f -> {
                try {
                    // Should not be blocking because allOf is done.
                    sb.append(f.get());
                } catch (Exception e) {
                    // exception type is also throwable!!
                    // Exception extends Throwable
                    output.completeExceptionally(e);
                }
            });
            output.complete(sb.toString());
        });

        return output;
    }

    private String callApi(String str) {
        // Stimulate RestTemplate.invoke(...)
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        return str.toUpperCase();
    }

}
