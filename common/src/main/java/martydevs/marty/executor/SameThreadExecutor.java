package martydevs.marty.executor;

import org.jspecify.annotations.NonNull;

import java.util.concurrent.Executor;

public final class SameThreadExecutor implements Executor {

    public static final SameThreadExecutor INSTANCE = new SameThreadExecutor();

    private SameThreadExecutor() {}

    @Override
    public void execute(@NonNull Runnable command) {
        command.run();
    }

}
