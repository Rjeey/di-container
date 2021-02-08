package by.rjeey;

import by.rjeey.annotation.Injector;
import by.rjeey.annotation.InjectorImpl;
import by.rjeey.annotation.Provider;
import by.rjeey.controller.EventController;
import by.rjeey.controller.EventControllerImpl;
import by.rjeey.dao.EventDao;
import by.rjeey.dao.EventDaoImpl;
import by.rjeey.service.EventService;
import by.rjeey.service.EventServiceImpl;
import by.rjeey.util.*;
import by.rjeey.exception.BindingNotFoundException;
import by.rjeey.exception.ConstructorNotFoundException;
import by.rjeey.exception.TooManyConstructorsException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InjectorTest {

    @Test
    public void testInjection() throws Exception {
        Injector injector = new InjectorImpl();
        injector.bind(EventDao.class, EventDaoImpl.class);
        injector.bind(EventService.class, EventServiceImpl.class);

        Provider<EventService> serviceProvider = injector.getProvider(EventService.class);

        Provider<EventDao> daoProvider = injector.getProvider(EventDao.class);
        
        assertNotNull(serviceProvider); // testing if Provider with @Inject constructor NN

        assertNotNull(daoProvider); // testing if Provider with default constructor NN

        assertNotNull(daoProvider.getInstance()); // testing if instance with default constructor NN

        assertNotNull(serviceProvider.getInstance()); // testing if instance with @Inject constructor NN

        assertNotNull(serviceProvider.getInstance().getDao()); // testing if field of injected instance NN

        assertSame(EventServiceImpl.class, serviceProvider.getInstance().getClass()); // testing if instance of Provider<EventService> is actually EventServiceImpl

        assertSame(EventDaoImpl.class, daoProvider.getInstance().getClass()); // testing if instance of Provider<EventDao> is actually EventDaoImpl
    }

    @Test
    public void testSingletonInjection() throws Exception { // the same as previous but with singletons
        Injector injector = new InjectorImpl();
        injector.bindSingleton(EventDao.class, EventDaoImpl.class);
        injector.bindSingleton(EventService.class, EventServiceImpl.class);

        Provider<EventService> serviceProvider = injector.getProvider(EventService.class);

        assertNotNull(serviceProvider);

        assertNotNull(serviceProvider.getInstance());

        assertNotNull(serviceProvider.getInstance().getDao());

        assertSame(EventServiceImpl.class, serviceProvider.getInstance().getClass());

        assertSame(EventDaoImpl.class, serviceProvider.getInstance().getDao().getClass());
    }

    @Test
    public void testExceptions() { // testing exceptions
        Injector injector = new InjectorImpl();
        injector.bind(EventDao.class, EventDaoImpl.class);
        injector.bind(ClassWithNoDefaultConstructor.class, ClassWithNoDefaultConstructorImpl.class);
        injector.bind(ClassWithManyConstructors.class, ClassWithManyConstructorsImpl.class);

        assertThrows(ConstructorNotFoundException.class, () -> injector.getProvider(ClassWithNoDefaultConstructor.class));

        assertThrows(BindingNotFoundException.class, () -> injector.getProvider(EventService.class));

        assertThrows(TooManyConstructorsException.class, () -> injector.getProvider(ClassWithManyConstructors.class));
    }

    @Test // testing many levels of injection EventController -> EventService -> EventDao, EventController also has SingletonClass field
    public void manyLevelsInjectionTest() throws Exception{
        Injector injector = new InjectorImpl();
        injector.bind(EventDao.class, EventDaoImpl.class);
        injector.bind(EventService.class, EventServiceImpl.class);
        injector.bind(EventController.class, EventControllerImpl.class);
        injector.bindSingleton(SingletonClass.class, SingletonClassImpl.class);

        Provider<EventController> controllerProvider = injector.getProvider(EventController.class);

        assertNotNull(controllerProvider);

        assertNotNull(controllerProvider.getInstance());

        assertNotNull(controllerProvider.getInstance().getService());

        assertNotNull(controllerProvider.getInstance().getService().getDao());

        assertNotNull(controllerProvider.getInstance().getSingleton());

        assertSame(EventControllerImpl.class, controllerProvider.getInstance().getClass());

        assertSame(EventServiceImpl.class, controllerProvider.getInstance().getService().getClass());

        assertSame(EventDaoImpl.class, controllerProvider.getInstance().getService().getDao().getClass());

        assertSame(SingletonClassImpl.class, controllerProvider.getInstance().getSingleton().getClass());
    }
}
