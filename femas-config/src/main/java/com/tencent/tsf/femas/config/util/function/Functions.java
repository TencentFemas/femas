package com.tencent.tsf.femas.config.util.function;

import com.google.common.base.Function;
import com.tencent.tsf.femas.config.exception.FemasConfigException;
import com.tencent.tsf.femas.config.util.parser.ParserException;
import com.tencent.tsf.femas.config.util.parser.Parsers;
import java.util.Date;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public interface Functions {

    Function<String, Integer> TO_INT_FUNCTION = new Function<String, Integer>() {
        @Override
        public Integer apply(String input) {
            return Integer.parseInt(input);
        }
    };

    Function<String, Long> TO_LONG_FUNCTION = new Function<String, Long>() {
        @Override
        public Long apply(String input) {
            return Long.parseLong(input);
        }
    };

    Function<String, Short> TO_SHORT_FUNCTION = new Function<String, Short>() {
        @Override
        public Short apply(String input) {
            return Short.parseShort(input);
        }
    };

    Function<String, Float> TO_FLOAT_FUNCTION = new Function<String, Float>() {
        @Override
        public Float apply(String input) {
            return Float.parseFloat(input);
        }
    };

    Function<String, Double> TO_DOUBLE_FUNCTION = new Function<String, Double>() {
        @Override
        public Double apply(String input) {
            return Double.parseDouble(input);
        }
    };

    Function<String, Byte> TO_BYTE_FUNCTION = new Function<String, Byte>() {
        @Override
        public Byte apply(String input) {
            return Byte.parseByte(input);
        }
    };

    Function<String, Boolean> TO_BOOLEAN_FUNCTION = new Function<String, Boolean>() {
        @Override
        public Boolean apply(String input) {
            return Boolean.parseBoolean(input);
        }
    };

    Function<String, Date> TO_DATE_FUNCTION = new Function<String, Date>() {
        @Override
        public Date apply(String input) {
            try {
                return Parsers.forDate().parse(input);
            } catch (ParserException ex) {
                throw new FemasConfigException("Parse date failed", ex);
            }
        }
    };

    Function<String, Long> TO_DURATION_FUNCTION = new Function<String, Long>() {
        @Override
        public Long apply(String input) {
            try {
                return Parsers.forDuration().parseToMillis(input);
            } catch (ParserException ex) {
                throw new FemasConfigException("Parse duration failed", ex);
            }
        }
    };
}
