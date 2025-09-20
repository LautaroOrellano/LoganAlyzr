import com.loganalizyr.collector.FileLogCollector;
import com.loganalizyr.processor.LogProcessor;

import java.util.List;


public class Main {
    public static void main(String[] args) {
        FileLogCollector collector = new FileLogCollector("logs.txt");
        LogProcessor processor = new LogProcessor("INFO");

        int offset = 0;
        int limit = 7;
        List<String> logs;
        int totalMatches = 0;

        do {
            logs = collector.readLogs(offset, limit);
            if (!logs.isEmpty()) {
                List<String> filtered = processor.filterLogs(logs);
                filtered.forEach(System.out::println);
                totalMatches += filtered.size();
                offset += logs.size();
            }
        } while (!logs.isEmpty());

        System.out.println("\n🔎 Total coincidencias encontradas: " + totalMatches);
    }

}