package com.loganalizyr.services.filterLog;

import com.loganalizyr.models.LogEntry;
import com.loganalizyr.services.LogFilterCriteria;

import java.util.List;

public interface IFilterLogs {

    List<LogEntry> filterLogs(List<LogEntry> logs, LogFilterCriteria criteria);

}
