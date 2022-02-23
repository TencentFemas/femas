package com.tencent.tsf.femas.adaptor.paas.logger;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import java.util.Map;

public class LogbackTraceConverter extends ClassicConverter implements ConverterBase {

    @Override
    public String convert(ILoggingEvent event) {
        Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();

        if (mdcPropertyMap == null) {
            return DEFAULT_VALUE;
        }

        StringBuffer stringBuffer = new StringBuffer("[");
        for (String key : MDC_KEYS) {
            String value = mdcPropertyMap.get(key);
            stringBuffer.append(",").append(value == null ? DEFAULT_SINGLE_VALUE : value);
        }
        stringBuffer.append("]");
        return stringBuffer.toString();
    }
}
