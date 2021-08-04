package cn.mengfly.springui;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import cn.mengfly.springui.ui.SpringUiModel;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Mengfly
 * @date 2021/8/3 16:48
 */
public class UiLogFilter extends Filter<ILoggingEvent> {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Level level;

    @Override
    public FilterReply decide(ILoggingEvent event) {

        String loggerName = event.getLoggerName();
        if (loggerName != null) {
            final Level level = event.getLevel();

            String formattedMessage = event.getFormattedMessage();
            String throwMessage = null;
            if (event.getThrowableProxy() != null) {
                throwMessage = event.getThrowableProxy().getMessage();
            }
            String showMsg = formattedMessage + (throwMessage == null ? "" : throwMessage);
            UiLog log = new UiLog(level, event.getTimeStamp(), showMsg);
            SpringUiModel.addLog(log);
        }
        return FilterReply.NEUTRAL;
    }

    public void setLevel(String level) {
        this.level = Level.toLevel(level);
    }


    @Override
    public void start() {
        if (this.level != null) {
            super.start();
        }
    }

    public static class UiLog {
        private final Level logType;
        private final Date timestamp;
        private final String content;

        public UiLog(Level logType, long timestamp, String content) {
            this.logType = logType;
            this.timestamp = new Date(timestamp);
            this.content = content;
        }

        public Level getLogType() {
            return logType;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public String getContent() {
            return content;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s \r %s\n", DATE_FORMAT.format(timestamp), logType.toString(), content);
        }

    }

}
