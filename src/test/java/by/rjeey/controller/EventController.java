package by.rjeey.controller;

import by.rjeey.util.SingletonClass;
import by.rjeey.service.EventService;

public interface EventController {

    EventService getService();

    SingletonClass getSingleton();
}
