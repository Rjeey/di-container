package by.rjeey.annotation;


import by.rjeey.exception.BindingNotFoundException;
import by.rjeey.exception.ConstructorNotFoundException;
import by.rjeey.exception.TooManyConstructorsException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class InjectorImpl implements Injector {

    private static final HashMap<Class<?>, Class<?>> bindings = new HashMap<>();

    private static final HashMap<Class<?>, Object> singletonBindings = new HashMap<>();

    /**
     * @param intf interface type of bean to inject implementation
     * @param <T>  implementation instance
     * @return Provider with instance
     */
    public synchronized <T> Provider<T> getProvider(final Class<T> intf) throws Exception {
        Class<?> type;


        if (bindings.get(intf) != null) {
            type = bindings.get(intf);
        } else if (singletonBindings.get(intf) != null) {
            return (Provider<T>) returnProvider(singletonBindings.get(intf));
        } else throw new BindingNotFoundException();

        final T finalObject = getObjectFromContainer(type);

        return returnProvider(finalObject);
    }

    /**
     * Puts interface and implementation inside DI container
     *
     * @param intf interface type of bean
     * @param impl implementation type of bean, should implement intf
     */
    public <T> void bind(Class<T> intf, Class<? extends T> impl) {
        getBindings().put(intf, impl);
    }

    /**
     * Creates an instance of implementation type and puts it in DI container
     *
     * @param intf interface type of bean
     * @param impl implementation type of bean, should implement intf
     */
    public <T> void bindSingleton(Class<T> intf, Class<? extends T> impl) throws Exception {
        Constructor<?> constructorToWork = null;
        Object o;
        for (Constructor<?> constr : impl.getConstructors()) {
            if (constr.getAnnotation(Inject.class) != null) {
                constructorToWork = constr;
            }
        }

        if (constructorToWork != null) {
            o = getObjectFromContainer(impl);
        } else {
            o = createObject(impl);
        }
        getSingletonBindings().put(intf, o);
    }

    private <T> Provider<T> returnProvider(T object) { // return Provider wit instance
        return () -> object;
    }

    /**
     * Get Class and return object from DI container
     *
     * @param type Interface of bean
     * @return instance of implementation type from container
     */
    private <T> T getObjectFromContainer(Class<?> type) throws Exception {
        Constructor<?> constructor = validateClass(type);

        Class<?>[] parameterTypes = constructor.getParameterTypes();

        List<Object> objects = new ArrayList<>();

        for (Class<?> parameterType : parameterTypes) {
            if (bindings.containsKey(parameterType)) {
                objects.add(createObject(bindings.get(parameterType)));
            } else if (singletonBindings.containsKey(parameterType)) {
                objects.add(singletonBindings.get(parameterType));
            }
        }

        return (T) constructor.newInstance(objects.toArray());
    }

    /**
     * Validates class for proper constructor and return it
     *
     * @param clazz implementation class
     * @return Constructor with @Inject annotation
     * @throws TooManyConstructorsException thrown if there are more than 1 @Inject constructor
     * @throws ConstructorNotFoundException thrown if neither @Inject constructor nor default constructor were found
     * @throws BindingNotFoundException     thrown if binding for interface class was not found
     */
    private Constructor<?> validateClass(Class<?> clazz) throws TooManyConstructorsException, ConstructorNotFoundException, BindingNotFoundException {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Constructor<?> constructorToWork = null;
        int counter = 0;
        for (Constructor<?> constructor : constructors) {
            if (constructor.getAnnotation(Inject.class) != null) {
                constructorToWork = constructor;
                counter++;
            }
        }
        if (counter > 1)
            throw new TooManyConstructorsException();
        if (constructorToWork == null) {
            List<Constructor<?>> constructorList = Arrays.stream(constructors)
                    .filter(constructor -> constructor.getParameterCount() == 0)
                    .collect(Collectors.toList());
            if (constructorList.size() != 0)
                constructorToWork = constructorList.get(0);
            else
                throw new ConstructorNotFoundException();
        }
        for (Class<?> parameter : constructorToWork.getParameterTypes()) {
            if (!bindings.containsKey(parameter) && !singletonBindings.containsKey(parameter))
                throw new BindingNotFoundException();
        }

        return constructorToWork;
    }

    /**
     * Creating object by class, this method is used to create params for injecting in constructors,
     * params may not have @Inject constructors
     */
    private Object createObject(Class<?> impl) throws Exception {
        Constructor<?> constructorToWork = null;
        for (Constructor<?> constr : impl.getConstructors()) { // Looking for @Inject constructors
            if (constr.getAnnotation(Inject.class) != null) {
                constructorToWork = constr;
            }
        }

        if (constructorToWork != null) {
            return getObjectFromContainer(impl);
        }

        List<Constructor<?>> defaultConstructor = Arrays.stream(impl.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0)
                .collect(Collectors.toList());
        if (defaultConstructor.size() != 0) {
            return defaultConstructor.get(0).newInstance();
        } else throw new ConstructorNotFoundException();
    }

    public static HashMap<Class<?>, Class<?>> getBindings() {
        return bindings;
    }

    public static HashMap<Class<?>, Object> getSingletonBindings() {
        return singletonBindings;
    }
}
