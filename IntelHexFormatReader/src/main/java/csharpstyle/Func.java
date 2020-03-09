package csharpstyle;

@FunctionalInterface
public interface Func<TInput, TResult> {
    TResult invoke(TInput target);
}
