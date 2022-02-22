package com.tencent.tsf.femas.adaptor.paas.logger;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.util.ReadOnlyStringMap;

@Plugin(name = "Log4J2TraceConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({"trace"})
public class Log4J2TraceConverter extends LogEventPatternConverter implements ConverterBase {

    private static final Log4J2TraceConverter INSTANCE =
            new Log4J2TraceConverter();

    private Log4J2TraceConverter() {
        super("trace", "trace");
    }

    public static Log4J2TraceConverter newInstance(
            final String[] options) {
        return INSTANCE;
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        final ReadOnlyStringMap contextData = event.getContextData();
        if (contextData == null || contextData.size() == 0) {
            toAppendTo.append(DEFAULT_VALUE);
            return;
        }
        StringBuffer stringBuffer = new StringBuffer("[");
        for (String key : MDC_KEYS) {
            String value = contextData.getValue(key);
            stringBuffer.append(",").append(value == null ? DEFAULT_SINGLE_VALUE : value);
        }
        stringBuffer.append("]");
        toAppendTo.append(stringBuffer.toString());
    }
}
