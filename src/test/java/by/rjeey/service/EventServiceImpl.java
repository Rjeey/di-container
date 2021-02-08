package by.rjeey.service;

import by.rjeey.annotation.Inject;
import by.rjeey.dao.EventDao;


public class EventServiceImpl implements EventService {

    EventDao eventDao;

    @Inject
    public EventServiceImpl(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    @Override
    public EventDao getDao() {
        return eventDao;
    }
}
