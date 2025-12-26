package com.loganalyzr.application;

import com.loganalyzr.core.ports.LogSource;
import com.loganalyzr.infrastructure.persistence.JsonLogReader;
import com.loganalyzr.infrastructure.persistence.SmartFileReader;

public class Main {
    public static void main(String[] args) {
        //String path = "logs.txt";
        String path = "logs.jsonl";

        //String logRegex = "(?<level>\\w+)\\s+\\[(?<date>.*?)\\]\\s+-\\s+(?<message>.*)";
        //String dateFormat = "yyyy-MM-dd HH:mm:ss";

        //SmartFileReader fileReader = new SmartFileReader(path, logRegex, dateFormat);

        LogSource fileReader = new JsonLogReader(path);

        Agent agent = new Agent(fileReader);

        System.out.println("Iniciando agent ...");
        agent.run();
    }

}