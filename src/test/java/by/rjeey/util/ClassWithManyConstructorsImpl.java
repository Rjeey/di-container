package by.rjeey.util;

import by.rjeey.annotation.Inject;
import by.rjeey.dao.EventDao;
import by.rjeey.service.EventService;

public class ClassWithManyConstructorsImpl implements ClassWithManyConstructors {

    EventDao eventDao;

    EventService eventService;

    @Inject
    public ClassWithManyConstructorsImpl(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    @Inject
    public ClassWithManyConstructorsImpl(EventService eventService) {
        this.eventService = eventService;
    }
}
