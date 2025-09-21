import com.loganalizyr.collector.FileLogCollector;
import com.loganalizyr.model.LogEntry;
import com.loganalizyr.processor.LogProcessor;
import com.loganalizyr.service.LogFilterCriteria;
import com.loganalizyr.service.filterLog.FilterLogsService;

import java.time.LocalDateTime;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        String filePath = "C:/";
        FileLogCollector collector = new FileLogCollector();
        List<LogEntry> logs = collector.loadLogs(filePath, 1,500);
        FilterLogsService service = new FilterLogsService();

        LogFilterCriteria criteria = new LogFilterCriteria();
        criteria.setStartDate(LocalDateTime.of(2025, 9, 1, 0, 0));
        criteria.setEndDate(LocalDateTime.of(2025, 9 ,  21, 23, 59));
        criteria.setLevel("Error");
        criteria.setKeyword("fallo");

        List<LogEntry> filteredLog = service.filterLogs(logs, criteria);
        filteredLog.forEach(System.out::println);
    }

}