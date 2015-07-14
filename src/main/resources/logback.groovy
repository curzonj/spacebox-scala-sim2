appender("STDOUT-1", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{5} - %msg%n"
    }
}

appender("STDOUT-2", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%msg%n"
    }
}

appender("STDOUT-3", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%-5level %logger{5} - %msg%n"
    }
}


root(DEBUG, [ "STDOUT-1" ])
logger("scredis", INFO)
logger("com.github.curzonj", TRACE)