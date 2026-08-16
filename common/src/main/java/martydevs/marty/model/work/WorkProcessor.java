package martydevs.marty.model.work;

import java.util.concurrent.CompletableFuture;

/**
 * Does some work and returns the result of it
 * @param <R> work result type
 * @param <W> work type
 */
public interface WorkProcessor<R, W extends Work<R>> {

    CompletableFuture<R> queue(W work);

}
