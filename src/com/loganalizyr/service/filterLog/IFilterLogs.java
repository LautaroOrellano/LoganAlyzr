package com.loganalizyr.service.filterLog;

import com.loganalizyr.model.LogEntry;
import com.loganalizyr.service.LogFilterCriteria;

import java.util.List;

public interface IFilterLogs {

    List<LogEntry> filterLogs(List<LogEntry> logs, LogFilterCriteria criteria);

}
