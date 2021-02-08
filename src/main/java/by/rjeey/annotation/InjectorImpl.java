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

        // Looking for bindings in hashmap, if exists, get implementation class
        if (bindings.get(intf) != null) {
            type = bindings.get(intf);
        } else if (singletonBindings.get(intf) != null) { // if it was not found in bindings, searching in singletons,
            return (Provider<T>) returnProvider(singletonBindings.get(intf)); // if found, return provider with instance
        } else throw new BindingNotFoundException(); // else throw exception

        final T finalObject = getObjectFromContainer(type); // if interface was found in bindings, create an object of implementation type

        return returnProvider(finalObject); // return provider with instance
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
        for (Constructor<?> constr : impl.getConstructors()) { // looking for constructors with @Inject annotation
            if (constr.getAnnotation(Inject.class) != null) {
                constructorToWork = constr;
            }
        }

        if (constructorToWork != null) { // if there is at least 1, create an instance of that class
            o = getObjectFromContainer(impl);
        } else { // else looking for constructor with no params
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
        Constructor<?> constructor = validateClass(type); // validating class and getting constructor to work with

        Class<?>[] parameterTypes = constructor.getParameterTypes();

        List<Object> objects = new ArrayList<>();

        for (Class<?> parameterType : parameterTypes) {
            if (bindings.containsKey(parameterType)) {
                objects.add(createObject(bindings.get(parameterType))); // for each parameter type creating an instance and adding it to list
            } else if (singletonBindings.containsKey(parameterType)) {
                objects.add(singletonBindings.get(parameterType)); // singleton instances are already in container
            }                                // idk how to realize lazy injection, so just get them from hashmap
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
        if (counter > 1) // if more than 1 @Inject constructors throw exception
            throw new TooManyConstructorsException();
        if (constructorToWork == null) { // if no @Inject constructors, look for defaultConstructors, if no, throw exception
            List<Constructor<?>> constructorList = Arrays.stream(constructors)
                    .filter(constructor -> constructor.getParameterCount() == 0)
                    .collect(Collectors.toList());
            if (constructorList.size() != 0)
                constructorToWork = constructorList.get(0);
            else
                throw new ConstructorNotFoundException();
        }
        for (Class<?> parameter : constructorToWork.getParameterTypes()) { // if parameter was not found in container, throw exception
            if (!bindings.containsKey(parameter) && !singletonBindings.containsKey(parameter))
                throw new BindingNotFoundException();
        }

        return constructorToWork; // return constructor in which instances will be injected
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
            return getObjectFromContainer(impl); // If there is one, params are injected in it from container
        }
        // looking for default constructor, if none was found, throw exception
        List<Constructor<?>> defaultConstructor = Arrays.stream(impl.getDeclaredConstructors())
                .filter(constructor -> constructor.getParameterCount() == 0)
                .collect(Collectors.toList());
        if (defaultConstructor.size() != 0) {
            return defaultConstructor.get(0).newInstance(); // create an instance using default constructor
        } else throw new ConstructorNotFoundException();
    }

    public static HashMap<Class<?>, Class<?>> getBindings() {
        return bindings;
    }

    public static HashMap<Class<?>, Object> getSingletonBindings() {
        return singletonBindings;
    }
}
