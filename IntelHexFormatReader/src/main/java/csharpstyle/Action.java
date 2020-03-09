package csharpstyle;

@FunctionalInterface
public interface Action<T> {
    void call(T target);
}
