package com.tencent.tsf.femas.gray.example.springcloud; /**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;


public class MapWeightRandomLoadBalanceTest {
    public static void main(String[] args) {
        test(1000);
    }

    public static void test(int totalCount) {
        long t = System.currentTimeMillis();

//        List<Pair<String, Double>> list = new ArrayList<Pair<String, Double>>();
//        list.add(new ImmutablePair<String, Double>("1.0", 10D));
//        list.add(new ImmutablePair<String, Double>("2.0", 50D));
//        list.add(new ImmutablePair<String, Double>("3.0", 20D));
//        list.add(new ImmutablePair<String, Double>("1.0", 10D));
//        list.add(new ImmutablePair<String, Double>("2.0", 50D));
//        list.add(new ImmutablePair<String, Double>("3.0", 20D));
//        list.add(new ImmutablePair<String, Double>("4.0", 5D));
//        list.add(new ImmutablePair<String, Double>("4.0", 5D));
//        list.add(new ImmutablePair<String, Double>("5.0", 15D));
//        list.add(new ImmutablePair<String, Double>("5.0", 15D));
        Map<String, Integer> a = new HashMap<String, Integer>();
        a.put("1.0", 10);
        a.put("2.0", 50);
        a.put("3.0", 20);
        a.put("4.0", 15);
        a.put("5.0", 5);


        int v1Count = 0;
        int v2Count = 0;
        int v3Count = 0;
        int v4Count = 0;
        int v5Count = 0;
        for (int i = 0; i < totalCount; i++) {
            String server = getLaneIdByPercentage(a);
            if (server.startsWith("1.0")) {
                v1Count++;
            }
            if (server.startsWith("2.0")) {
                v2Count++;
            }
            if (server.startsWith("3.0")) {
                v3Count++;
            }
            if (server.startsWith("4.0")) {
                v4Count++;
            }
            if (server.startsWith("5.0")) {
                v5Count++;
            }
        }

        System.out.println("------------------------------");
        System.out.println(totalCount + "次循环，散列方式随机权重准确度和性能：");
        DecimalFormat format = new DecimalFormat("0.0000");
        System.out.println("1.0版本服务随机权重=" + format.format((double) v1Count * 100 / totalCount) + "%");
        System.out.println("2.0版本服务随机权重=" + format.format((double) v2Count * 100 / totalCount) + "%");
        System.out.println("3.0版本服务随机权重=" + format.format((double) v3Count * 100 / totalCount) + "%");
        System.out.println("4.0版本服务随机权重=" + format.format((double) v4Count * 100 / totalCount) + "%");
        System.out.println("5.0版本服务随机权重=" + format.format((double) v5Count * 100 / totalCount) + "%");
        System.out.println("耗时时间：" + (System.currentTimeMillis() - t));
        System.out.println("------------------------------");
    }
    
    
    private static String getLaneIdByPercentage(Map<String, Integer> laneMap) {
        Map<Integer, String> reverseMap = new HashMap<>();
        List<Integer> integers = new ArrayList<>();
        int current = 0;
        for (Map.Entry<String, Integer> entry : laneMap.entrySet()) {
            current += entry.getValue();
            reverseMap.put(current, entry.getKey());
            integers.add(current);
        }
        Collections.sort(integers, Comparator.comparingInt(r -> r));
        Random random = new Random();
        int index = ThreadLocalRandom.current().nextInt(100);
       // Integer index = random.nextInt(100);
        int cur;
        int res = 0;
        int before = 0;
        for (int a = 0; a < integers.size(); a++) {
            cur = integers.get(a);
            if (a <3) {
                if (index <= cur) {
                    res = integers.get(a);
                }
                if (a == 2){
                    before = res;
                }
            } else {
                if (before < cur && index <= cur){
                    res = integers.get(a);
                    before = res;
                }
            }

            if (res > 0) {
                return reverseMap.get(res);
            }
        }
        return reverseMap.get(res);
    }
}