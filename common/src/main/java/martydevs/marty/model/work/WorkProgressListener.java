package martydevs.marty.model.work;

@FunctionalInterface
public interface WorkProgressListener<R, W extends Work<R>> {

    /**
     * Called from {@link WorkProcessor} on update of processing {@link Work}
     * @param work work being done
     * @param progress -1 means that work is registered in the processor and will be processed soon.
     *                 0 means that work has started.
     *                 Values from 0 to 1 (exclusive) means the completion state of the work.
     *                 1 means the work is complete.
     */
    void update(W work, float progress);

}
