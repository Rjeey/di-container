package by.rjeey.controller;

import by.rjeey.annotation.Inject;
import by.rjeey.util.SingletonClass;
import by.rjeey.service.EventService;


public class EventControllerImpl implements EventController {

    EventService eventService;

    SingletonClass singletonClass;

    @Inject
    public EventControllerImpl(EventService eventService, SingletonClass singletonClass) {
        this.eventService = eventService;
        this.singletonClass = singletonClass;
    }

    @Override
    public EventService getService() {
        return eventService;
    }

    @Override
    public SingletonClass getSingleton() {
        return singletonClass;
    }
}
