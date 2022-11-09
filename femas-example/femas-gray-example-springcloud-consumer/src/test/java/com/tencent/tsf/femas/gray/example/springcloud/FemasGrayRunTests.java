package com.tencent.tsf.femas.gray.example.springcloud;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

public class FemasGrayRunTests {
    public static void main(String[] args) throws URISyntaxException {
        String url = "http://127.0.0.1:29004/get";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders header = new HttpHeaders();
        // 需求需要传参为form-data格式
        header.setContentType(MediaType.MULTIPART_FORM_DATA);
        header.set("femas-ut-g", "1");

        RequestEntity<Void> build = RequestEntity.get(URI.create(url))
                .header("femas-ut-g", "1").build();
        double countV1 = 0 ;
        double countV2 = 0 ;

        for (int i =0 ; i < 10000 ; i ++ ){
            ResponseEntity<String> exchange = restTemplate.exchange(build, String.class);
            String body = exchange.getBody();

            if (body.contains("[application=femas-gray-example-cloud-provider,port=29003,version=2.0]")) {
                countV1 ++ ;
            }
            if (body.contains("[application=femas-gray-example-cloud-provider,port=19003,version=1.0]")) {
                countV2 ++ ;
            }
            System.out.println(body);
        }

        System.out.println("countV2::::::::::::::" + (countV2 / 100) + "%");
        System.out.println("countV1::::::::::::::" + (countV1/ 100) + "%");

    }

//    public static void main(String[] args) {
//        double countV1 = 0 ;
//        double countV2 = 0 ;
//
//        Map<String, Integer> map = new HashMap<>();
//        map.put("1.0",10);
//        map.put("2.0", 90);
//
//        for (int i =0 ; i < 100000 ; i ++ ){
//            String random = random(map);
//            if (random.contains("1.0")) {
//                countV1 ++ ;
//            }
//            if (random.contains("2.0")) {
//                countV2 ++ ;
//            }
//            System.out.println(random);
//        }
//
//        System.out.println("countV2::::::::::::::" + (countV2 / 1000) + "%");
//        System.out.println("countV1::::::::::::::" + (countV1/ 1000) + "%");
//    }
//
//    public static String random(Map<String, Integer> laneMap){
//        TreeMap<Integer, String> weightMap = new TreeMap<>();
//        List<Integer> streamNumbers = new ArrayList<>();
//        int cur = 0;
//        for (Map.Entry<String, Integer> entry : laneMap.entrySet()) {
//            cur += entry.getValue();
//            weightMap.put(cur, entry.getKey());
//        }
//
//        Collections.sort(streamNumbers, Comparator.comparingInt(r -> r));
//        Random random = new Random();
//        Integer index = random.nextInt(100);
//
//        SortedMap<Integer, String> tailMap = weightMap.tailMap(index, false);
//        return  weightMap.get(tailMap.firstKey());
//
//    }
}
