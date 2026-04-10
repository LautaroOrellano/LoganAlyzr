package com.loganalyzr.core.ports;

import com.loganalyzr.core.model.Event;

import java.util.List;

public interface ReportPublisher {

    void publish(List<Event> events);
}
