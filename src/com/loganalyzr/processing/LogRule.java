package com.loganalyzr.processing;

import com.loganalyzr.models.LogEvent;

import java.util.List;

public interface LogRule {

    List<LogEvent> filterLogs(List<LogEvent> logs, FilterRules criteria);

}
