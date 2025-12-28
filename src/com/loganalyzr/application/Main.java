package com.loganalyzr.application;

import com.loganalyzr.core.ports.LogSource;
import com.loganalyzr.core.ports.ReportPublisher;
import com.loganalyzr.infrastructure.persistence.JsonLogReader;
import com.loganalyzr.infrastructure.persistence.SmartFileReader;
import com.loganalyzr.infrastructure.ui.ConsoleReporter;

public class Main {
    public static void main(String[] args) {
        LogSource source = new JsonLogReader("logs.jsonl");
        ReportPublisher reporter = new ConsoleReporter();

        Agent agent = new Agent(source, reporter);

        agent.run();
    }
}