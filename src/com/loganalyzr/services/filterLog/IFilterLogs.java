package com.loganalyzr.services.filterLog;

import com.loganalyzr.models.LogEntry;
import com.loganalyzr.services.LogFilterCriteria;

import java.util.List;

public interface IFilterLogs {

    List<LogEntry> filterLogs(List<LogEntry> logs, LogFilterCriteria criteria);

}
