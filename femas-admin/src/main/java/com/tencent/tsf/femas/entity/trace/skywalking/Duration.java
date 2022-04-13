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

import java.text.SimpleDateFormat;
import java.util.Date;

import com.tencent.tsf.femas.common.util.StringUtils;
import lombok.Getter;
import lombok.Setter;


/**
 * @Author leoziltong@tencent.com
 * @Date: 2021/8/6 16:57
 */

@Getter
@Setter
public class Duration {

    private Long start;
    private Long end;

    public Duration() {
    }

    public Duration(Long start, Long end) {
        this.start = start;
        this.end = end;
    }

    public String getStartTimeBucket() {

        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(start)).replaceAll(":", "");
    }

    public String getEndTimeBucket() {

        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(end)).replaceAll(":", "");
    }

    public boolean validate(){
        if (this.start == null || this.start == 0L || this.end == null || this.end == 0L) {
            return false;
        }
        return true;
    }

    public String getStartDayBucket() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(start));
    }

    public String getEndDayBucket() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(end));
    }

//
//    /**
//     * See {@link DurationUtils#convertToTimeBucket(String)}
//     */
//    public long getStartTimeBucket() {
//        return DurationUtils.INSTANCE.convertToTimeBucket(start);
//    }
//
//    /**
//     * See {@link DurationUtils#convertToTimeBucket(String)}
//     */
//    public long getEndTimeBucket() {
//        return DurationUtils.INSTANCE.convertToTimeBucket(end);
//    }
//
//    public long getStartTimestamp() {
//        return DurationUtils.INSTANCE.startTimeToTimestamp(step, start);
//    }
//
//    public long getEndTimestamp() {
//        return DurationUtils.INSTANCE.endTimeToTimestamp(step, end);
//    }
//
//    public long getStartTimeBucketInSec() {
//        return DurationUtils.INSTANCE.startTimeDurationToSecondTimeBucket(step, start);
//    }
//
//    public long getEndTimeBucketInSec() {
//        return DurationUtils.INSTANCE.endTimeDurationToSecondTimeBucket(step, end);
//    }
//
//    /**
//     * Assemble time point based on {@link #step} and {@link #start} / {@link #end}
//     */
//    public List<PointOfTime> assembleDurationPoints() {
//        return DurationUtils.INSTANCE.getDurationPoints(step, getStartTimeBucket(), getEndTimeBucket());
//    }
}
