package com.loganalyzr.core.ports;

import com.loganalyzr.core.model.LogEvent;

public interface LogRule {

     boolean test(LogEvent event);

}
