package com.loganalyzr.core.ports;

import com.loganalyzr.core.model.LogEvent;

import java.util.List;

public interface ReportPublisher {

    void publish(List<LogEvent> events);
}
