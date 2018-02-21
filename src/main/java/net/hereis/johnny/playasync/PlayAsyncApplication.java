package net.hereis.johnny.playasync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableAsync
public class PlayAsyncApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		// close the application context to shut down
		// the custom ExecutorService
		SpringApplication.run(PlayAsyncApplication.class, args).close();
	}

	@Bean
	public Executor asyncExecutor() {
		ThreadPoolTaskExecutor poolTaskExecutor = new ThreadPoolTaskExecutor();
		poolTaskExecutor.setCorePoolSize(2);
		poolTaskExecutor.setMaxPoolSize(2);
		poolTaskExecutor.setQueueCapacity(500);
		poolTaskExecutor.setThreadNamePrefix("GithubLookup-");
		poolTaskExecutor.initialize();
		return poolTaskExecutor;
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(PlayAsyncApplication.class);
	}
}
