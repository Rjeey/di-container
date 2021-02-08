package by.rjeey.annotation;

public interface Injector {

    <T> Provider<T> getProvider(Class<T> type) throws Exception ;
    <T> void bind(Class<T> init, Class<? extends T> impl);
    <T> void bindSingleton(Class<T> init, Class<? extends T> impl)throws Exception ;
}
