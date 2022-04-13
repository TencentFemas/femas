/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.tsf.femas.entity.trace.skywalking;

import com.tencent.tsf.femas.common.exception.FemasRuntimeException;
import java.util.LinkedList;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/8/6 17:37
 */
public enum DurationUtils {
    INSTANCE;

    private static final DateTimeFormatter YYYY_MM_DD = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final DateTimeFormatter YYYY_MM_DD_HH = DateTimeFormat.forPattern("yyyy-MM-dd HH");
    private static final DateTimeFormatter YYYY_MM_DD_HHMM = DateTimeFormat.forPattern("yyyy-MM-dd HHmm");
    private static final DateTimeFormatter YYYY_MM_DD_HHMMSS = DateTimeFormat.forPattern("yyyy-MM-dd HHmmss");

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormat.forPattern("yyyyMMdd");
    private static final DateTimeFormatter YYYYMMDDHH = DateTimeFormat.forPattern("yyyyMMddHH");
    private static final DateTimeFormatter YYYYMMDDHHMM = DateTimeFormat.forPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    /**
     * Convert date in `yyyy-MM-dd HHmmss` style to `yyyyMMddHHmmss` no matter the precision. Such as, in day precision,
     * this covert `yyyy-MM-dd` style to `yyyyMMdd`.
     */
    public long convertToTimeBucket(String dateStr) {
        dateStr = dateStr.replaceAll("-", "");
        dateStr = dateStr.replaceAll(" ", "");
        return Long.parseLong(dateStr);
    }

    public long startTimeDurationToSecondTimeBucket(Step step, String dateStr) {
        long secondTimeBucket = 0;
        switch (step) {
            case DAY:
                secondTimeBucket = convertToTimeBucket(dateStr) * 100 * 100 * 100;
                break;
            case HOUR:
                secondTimeBucket = convertToTimeBucket(dateStr) * 100 * 100;
                break;
            case MINUTE:
                secondTimeBucket = convertToTimeBucket(dateStr) * 100;
                break;
            case SECOND:
                secondTimeBucket = convertToTimeBucket(dateStr);
                break;
        }
        return secondTimeBucket;
    }

    public long endTimeDurationToSecondTimeBucket(Step step, String dateStr) {
        long secondTimeBucket = 0;
        switch (step) {
            case DAY:
                secondTimeBucket = ((convertToTimeBucket(dateStr) * 100 + 99) * 100 + 99) * 100 + 99;
                break;
            case HOUR:
                secondTimeBucket = (convertToTimeBucket(dateStr) * 100 + 99) * 100 + 99;
                break;
            case MINUTE:
                secondTimeBucket = convertToTimeBucket(dateStr) * 100 + 99;
                break;
            case SECOND:
                secondTimeBucket = convertToTimeBucket(dateStr);
                break;
        }
        return secondTimeBucket;
    }

    public List<PointOfTime> getDurationPoints(Step step, long startTimeBucket, long endTimeBucket) {
        DateTime dateTime = parseToDateTime(step, startTimeBucket);

        List<PointOfTime> durations = new LinkedList<>();
        durations.add(new PointOfTime(startTimeBucket));

        int i = 0;
        do {
            switch (step) {
                case DAY:
                    dateTime = dateTime.plusDays(1);
                    String timeBucket = YYYYMMDD.print(dateTime);
                    durations.add(new PointOfTime(Long.parseLong(timeBucket)));
                    break;
                case HOUR:
                    dateTime = dateTime.plusHours(1);
                    timeBucket = YYYYMMDDHH.print(dateTime);
                    durations.add(new PointOfTime(Long.parseLong(timeBucket)));
                    break;
                case MINUTE:
                    dateTime = dateTime.plusMinutes(1);
                    timeBucket = YYYYMMDDHHMM.print(dateTime);
                    durations.add(new PointOfTime(Long.parseLong(timeBucket)));
                    break;
                case SECOND:
                    dateTime = dateTime.plusSeconds(1);
                    timeBucket = YYYYMMDDHHMMSS.print(dateTime);
                    durations.add(new PointOfTime(Long.parseLong(timeBucket)));
                    break;
            }
            i++;
            if (i > 500) {
                throw new FemasRuntimeException(
                        "Duration data error, step: " + step.name() + ", start: " + startTimeBucket + ", end: "
                                + endTimeBucket);
            }
        }
        while (endTimeBucket != durations.get(durations.size() - 1).getPoint());

        return durations;
    }

    public long startTimeToTimestamp(Step step, String dateStr) {
        switch (step) {
            case DAY:
                return YYYY_MM_DD.parseMillis(dateStr);
            case HOUR:
                return YYYY_MM_DD_HH.parseMillis(dateStr);
            case MINUTE:
                return YYYY_MM_DD_HHMM.parseMillis(dateStr);
            case SECOND:
                return YYYY_MM_DD_HHMMSS.parseMillis(dateStr);
        }
        throw new FemasRuntimeException("Unsupported step " + step.name());
    }

    public long endTimeToTimestamp(Step step, String dateStr) {
        switch (step) {
            case DAY:
                return YYYY_MM_DD.parseDateTime(dateStr).plusDays(1).getMillis();
            case HOUR:
                return YYYY_MM_DD_HH.parseDateTime(dateStr).plusHours(1).getMillis();
            case MINUTE:
                return YYYY_MM_DD_HHMM.parseDateTime(dateStr).plusMinutes(1).getMillis();
            case SECOND:
                return YYYY_MM_DD_HHMMSS.parseDateTime(dateStr).plusSeconds(1).getMillis();
        }
        throw new FemasRuntimeException("Unsupported step " + step.name());
    }

    private DateTime parseToDateTime(Step step, long time) {
        switch (step) {
            case DAY:
                return YYYYMMDD.parseDateTime(String.valueOf(time));
            case HOUR:
                return YYYYMMDDHH.parseDateTime(String.valueOf(time));
            case MINUTE:
                return YYYYMMDDHHMM.parseDateTime(String.valueOf(time));
            case SECOND:
                return YYYYMMDDHHMMSS.parseDateTime(String.valueOf(time));
        }
        throw new FemasRuntimeException("Unexpected downsampling: " + step.name());
    }
}