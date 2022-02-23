package com.tencent.tsf.femas.config.internals;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.collect.Maps;
import com.tencent.tsf.femas.config.util.function.Functions;
import com.tencent.tsf.femas.config.util.parser.Parsers;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhixinzxliu
 */
public abstract class AbstractStringConfig extends AbstractConfig<String> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractStringConfig.class);
    private final Map<String, Cache<String, String[]>> arrayCache;
    private volatile Cache<String, Integer> integerCache;
    private volatile Cache<String, Long> longCache;
    private volatile Cache<String, Short> shortCache;
    private volatile Cache<String, Float> floatCache;
    private volatile Cache<String, Double> doubleCache;
    private volatile Cache<String, Byte> byteCache;
    private volatile Cache<String, Boolean> booleanCache;
    private volatile Cache<String, Date> dateCache;
    private volatile Cache<String, Long> durationCache;

    public AbstractStringConfig() {
        arrayCache = Maps.newConcurrentMap();
    }

    /**
     * Return the integer property value with the given key, or {@code defaultValue} if the key
     * doesn't exist.
     *
     * @param key the property name
     * @param defaultValue the default value when key is not found or any error occurred
     * @return the property value as integer
     */
    public Integer getIntProperty(String key, Integer defaultValue) {
        try {
            if (integerCache == null) {
                synchronized (this) {
                    if (integerCache == null) {
                        integerCache = newCache();
                    }
                }
            }

            return getValueFromCache(key, Functions.TO_INT_FUNCTION, integerCache, defaultValue);
        } catch (Throwable ex) {
            logger.error("getIntProperty for %s failed, return default value %d", key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Return the long property value with the given key, or {@code defaultValue} if the key doesn't
     * exist.
     *
     * @param key the property name
     * @param defaultValue the default value when key is not found or any error occurred
     * @return the property value as long
     */
    public Long getLongProperty(String key, Long defaultValue) {
        try {
            if (longCache == null) {
                synchronized (this) {
                    if (longCache == null) {
                        longCache = newCache();
                    }
                }
            }

            return getValueFromCache(key, Functions.TO_LONG_FUNCTION, longCache, defaultValue);
        } catch (Throwable ex) {
            logger.error("getLongProperty for %s failed, return default value %d", key, defaultValue);
        }
        return defaultValue;
    }


    /**
     * Return the short property value with the given key, or {@code defaultValue} if the key doesn't
     * exist.
     *
     * @param key the property name
     * @param defaultValue the default value when key is not found or any error occurred
     * @return the property value as short
     */
    public Short getShortProperty(String key, Short defaultValue) {
        try {
            if (shortCache == null) {
                synchronized (this) {
                    if (shortCache == null) {
                        shortCache = newCache();
                    }
                }
            }

            return getValueFromCache(key, Functions.TO_SHORT_FUNCTION, shortCache, defaultValue);
        } catch (Throwable ex) {
            logger.error("getShortProperty for %s failed, return default value %d", key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Return the float property value with the given key, or {@code defaultValue} if the key doesn't
     * exist.
     *
     * @param key the property name
     * @param defaultValue the default value when key is not found or any error occurred
     * @return the property value as float
     */
    public Float getFloatProperty(String key, Float defaultValue) {
        try {
            if (floatCache == null) {
                synchronized (this) {
                    if (floatCache == null) {
                        floatCache = newCache();
                    }
                }
            }

            return getValueFromCache(key, Functions.TO_FLOAT_FUNCTION, floatCache, defaultValue);
        } catch (Throwable ex) {
            logger.error("getFloatProperty for %s failed, return default value %f", key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Return the double property value with the given key, or {@code defaultValue} if the key doesn't
     * exist.
     *
     * @param key the property name
     * @param defaultValue the default value when key is not found or any error occurred
     * @return the property value as double
     */
    public Double getDoubleProperty(String key, Double defaultValue) {
        try {
            if (doubleCache == null) {
                synchronized (this) {
                    if (doubleCache == null) {
                        doubleCache = newCache();
                    }
                }
            }

            return getValueFromCache(key, Functions.TO_DOUBLE_FUNCTION, doubleCache, defaultValue);
        } catch (Throwable ex) {
            logger.error("getDoubleProperty for %s failed, return default value %f", key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Return the byte property value with the given key, or {@code defaultValue} if the key doesn't
     * exist.
     *
     * @param key the property name
     * @param defaultValue the default value when key is not found or any error occurred
     * @return the property value as byte
     */
    public Byte getByteProperty(String key, Byte defaultValue) {
        try {
            if (byteCache == null) {
                synchronized (this) {
                    if (byteCache == null) {
                        byteCache = newCache();
                    }
                }
            }

            return getValueFromCache(key, Functions.TO_BYTE_FUNCTION, byteCache, defaultValue);
        } catch (Throwable ex) {
            logger.error("getByteProperty for %s failed, return default value %d", key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Return the boolean property value with the given key, or {@code defaultValue} if the key
     * doesn't exist.
     *
     * @param key the property name
     * @param defaultValue the default value when key is not found or any error occurred
     * @return the property value as boolean
     */
    public Boolean getBooleanProperty(String key, Boolean defaultValue) {
        try {
            if (booleanCache == null) {
                synchronized (this) {
                    if (booleanCache == null) {
                        booleanCache = newCache();
                    }
                }
            }

            return getValueFromCache(key, Functions.TO_BOOLEAN_FUNCTION, booleanCache, defaultValue);
        } catch (Throwable ex) {
            logger.error("getBooleanProperty for %s failed, return default value %b", key, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Return the array property value with the given key, or {@code defaultValue} if the key doesn't exist.
     *
     * @param key the property name
     * @param delimiter the delimiter regex
     * @param defaultValue the default value when key is not found or any error occurred
     */
    public String[] getArrayProperty(String key, final String delimiter, String[] defaultValue) {
        try {
            if (!arrayCache.containsKey(delimiter)) {
                synchronized (this) {
                    if (!arrayCache.containsKey(delimiter)) {
                        arrayCache.put(delimiter, this.<String[]>newCache());
                    }
                }
            }

            Cache<String, String[]> cache = arrayCache.get(delimiter);
            String[] result = cache.getIfPresent(key);

            if (result != null) {
                return result;
            }

            return getValueAndStoreToCache(key, new Function<String, String[]>() {
                @Override
                public String[] apply(String input) {
                    return input.split(delimiter);
                }
            }, cache, defaultValue);
        } catch (Throwable ex) {
            logger.error("getArrayProperty for %s failed, return default value", key);
        }
        return defaultValue;
    }

    /**
     * Return the Enum property value with the given key, or {@code defaultValue} if the key doesn't exist.
     *
     * @param key the property name
     * @param enumType the enum class
     * @param defaultValue the default value when key is not found or any error occurred
     * @param <T> the enum
     * @return the property value
     */
    public <T extends Enum<T>> T getEnumProperty(String key, Class<T> enumType, T defaultValue) {
        try {
            String value = getProperty(key, null);

            if (value != null) {
                return Enum.valueOf(enumType, value);
            }
        } catch (Throwable ex) {
            logger.error("getEnumProperty for %s failed, return default value %s", key, defaultValue);
        }

        return defaultValue;
    }

    /**
     * Return the Date property value with the given name, or {@code defaultValue} if the name doesn't exist.
     * Will try to parse the date with Locale.US and formats as follows: yyyy-MM-dd HH:mm:ss.SSS,
     * yyyy-MM-dd HH:mm:ss and yyyy-MM-dd
     *
     * @param key the property name
     * @param defaultValue the default value when name is not found or any error occurred
     * @return the property value
     */
    public Date getDateProperty(String key, Date defaultValue) {
        try {
            if (dateCache == null) {
                synchronized (this) {
                    if (dateCache == null) {
                        dateCache = newCache();
                    }
                }
            }

            return getValueFromCache(key, Functions.TO_DATE_FUNCTION, dateCache, defaultValue);
        } catch (Throwable ex) {
            logger.error("getDateProperty for %s failed, return default value %s", key, defaultValue);
        }

        return defaultValue;
    }

    /**
     * Return the Date property value with the given name, or {@code defaultValue} if the name doesn't exist.
     * Will parse the date with the format specified and Locale.US
     *
     * @param key the property name
     * @param format the date format, see {@link java.text.SimpleDateFormat} for more
     *         information
     * @param defaultValue the default value when name is not found or any error occurred
     * @return the property value
     */
    public Date getDateProperty(String key, String format, Date defaultValue) {
        try {
            String value = getProperty(key, null);

            if (value != null) {
                return Parsers.forDate().parse(value, format);
            }
        } catch (Throwable ex) {
            logger.error("getDateProperty for %s failed, return default value %s", key, defaultValue);
        }

        return defaultValue;
    }

    /**
     * Return the Date property value with the given name, or {@code defaultValue} if the name doesn't exist.
     *
     * @param key the property name
     * @param format the date format, see {@link java.text.SimpleDateFormat} for more
     *         information
     * @param locale the locale to use
     * @param defaultValue the default value when name is not found or any error occurred
     * @return the property value
     */
    public Date getDateProperty(String key, String format, Locale locale, Date defaultValue) {
        try {
            String value = getProperty(key, null);

            if (value != null) {
                return Parsers.forDate().parse(value, format, locale);
            }
        } catch (Throwable ex) {
            logger.error("getDateProperty for %s failed, return default value %s", key, defaultValue);
        }

        return defaultValue;
    }

    /**
     * Return the duration property value(in milliseconds) with the given name, or {@code
     * defaultValue} if the name doesn't exist. Please note the format should comply with the follow
     * example (case insensitive). Examples:
     * <pre>
     *    "123MS"          -- parses as "123 milliseconds"
     *    "20S"            -- parses as "20 seconds"
     *    "15M"            -- parses as "15 minutes" (where a minute is 60 seconds)
     *    "10H"            -- parses as "10 hours" (where an hour is 3600 seconds)
     *    "2D"             -- parses as "2 days" (where a day is 24 hours or 86400 seconds)
     *    "2D3H4M5S123MS"  -- parses as "2 days, 3 hours, 4 minutes, 5 seconds and 123 milliseconds"
     * </pre>
     *
     * @param key the property name
     * @param defaultValue the default value when name is not found or any error occurred
     * @return the parsed property value(in milliseconds)
     */
    public long getDurationProperty(String key, long defaultValue) {
        try {
            if (durationCache == null) {
                synchronized (this) {
                    if (durationCache == null) {
                        durationCache = newCache();
                    }
                }
            }

            return getValueFromCache(key, Functions.TO_DURATION_FUNCTION, durationCache, defaultValue);
        } catch (Throwable ex) {
            logger.error("getDurationProperty for %s failed, return default value %d", key, defaultValue);
        }

        return defaultValue;
    }

    @Override
    public <T> T getProperty(String key, Function<String, T> function, T defaultValue) {
        try {
            String value = getProperty(key, null);

            if (value != null) {
                return function.apply(value);
            }
        } catch (Throwable ex) {
            logger.error("getProperty for %s failed, return default value %s", key, defaultValue);
        }

        return defaultValue;
    }

    private <T> T getValueFromCache(String key, Function<String, T> parser, Cache<String, T> cache, T defaultValue) {
        T result = cache.getIfPresent(key);

        if (result != null) {
            return result;
        }

        return getValueAndStoreToCache(key, parser, cache, defaultValue);
    }

    private <T> T getValueAndStoreToCache(String key, Function<String, T> parser, Cache<String, T> cache,
            T defaultValue) {
        String value = getProperty(key, null);

        if (value != null) {
            T result = parser.apply(value);

            if (result != null) {
                synchronized (this) {
                    cache.put(key, result);
                }
                return result;
            }
        }

        return defaultValue;
    }
}
