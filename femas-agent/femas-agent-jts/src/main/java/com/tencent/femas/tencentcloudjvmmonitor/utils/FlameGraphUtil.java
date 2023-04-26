/**
 * Copyright 2010-2021 the original author or authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.femas.tencentcloudjvmmonitor.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 火焰图数据解析工具类
 *
 * @author v_lquanlin
 */
public class FlameGraphUtil {
    private static final Logger LOGGER = Logger.getLogger(FlameGraphUtil.class);
    private static ObjectMapper mapper = new ObjectMapper();

    public static String parseJsonFromString(String rawString) throws IOException {
        if (rawString == null || rawString.length() == 0) {
            return null;
        }

        long b;
        b = System.currentTimeMillis();
        List<String> metadata = new ArrayList<>();

        Reader inputString = new StringReader(rawString);
        BufferedReader bufferedReader = new BufferedReader(inputString);


        String l;

        while ((l = bufferedReader.readLine()) != null) {
            //LOGGER.debug("read line from file: " + l);
            metadata.add(l);
        }

        LOGGER.debug("metaData size is: " + metadata.size());

        FlameGraphNode rootNode = new FlameGraphNode();
        rootNode.setName("root");

        int rootValue = 0;
        int maxTier = 0;
        //int objectCount = 0; //TODO 记录产生的对象数量 可删除
        List<LinkedList<FlameGraphNode>> nodeChainList = new ArrayList<>();

        for (int j = 0; j < metadata.size(); j++) {
            String line = metadata.get(j);
            int num = Integer.valueOf(line.substring(line.lastIndexOf(" ") + 1));
            line = line.substring(0, line.lastIndexOf(" "));
            String[] elements = line.split("\\|");

            LinkedList<FlameGraphNode> nodeChain = new LinkedList<>();
            nodeChainList.add(nodeChain);
            for (int i = 1; i <= elements.length; i++) {
                FlameGraphNode node = new FlameGraphNode(elements[i - 1],num,metadata.size() - j,metadata.size() - j);
                nodeChain.add(node);


                //objectCount++; //TODO 记录产生的对象数量 可删除
                if (i == 1) {
                    node.setPreName(rootNode.getName());
                } else {
                    node.setPreName(elements[i - 2]);
                }

            }
            if (maxTier < elements.length) {
                maxTier = elements.length;
            }
            rootValue += num;
        }

        List<List<FlameGraphNode>> tierNodesList = new ArrayList<>();
        for (int i = 0; i < maxTier; i++) {
            List<FlameGraphNode> thisList = new ArrayList<>();
            //objectCount++; //TODO 记录产生的对象数量 可删除
            for (LinkedList<FlameGraphNode> nodeChain : nodeChainList) {
                if (nodeChain.size() >= i + 1) {
                    FlameGraphNode node = nodeChain.get(i);
                    thisList.add(node);
                }
            }

            LinkedList<FlameGraphNode> mergeList = new LinkedList<>();
            tierNodesList.add(mergeList);
           // objectCount++; //TODO 记录产生的对象数量 可删除

            for (FlameGraphNode node : thisList) {
                if (mergeList.size() == 0 || !mergeList.getLast().getName().equals(node.getName())) {
                    // mergeList.add(node.deepClone());
                    mergeList.add(node);
                } else {
                    FlameGraphNode last = mergeList.getLast();
                    if (node.getyHigh().equals(last.getyLow() - 1) && node.getPreName().equals(last.getPreName())) {
                        last.setyLow(node.getyLow());
                        last.setValue(last.getValue() + node.getValue());
                    } else {
                        // mergeList.add(node.deepClone());
                        mergeList.add(node);
                    }
                }
            }
        }

        for (int i = 0; i < tierNodesList.size(); i++) {
            List<FlameGraphNode> thisTierNodes = tierNodesList.get(i);
            if (i == 0) {
                rootNode.setChildren(thisTierNodes);
            }

            List<FlameGraphNode> nextTierNodes;
            if (i < tierNodesList.size() - 1) {
                nextTierNodes = tierNodesList.get(i + 1);
            } else {
                break;
            }

            for (FlameGraphNode thisTierNode : thisTierNodes) {
                List<FlameGraphNode> children = new ArrayList<>();
                //objectCount++; //TODO 记录产生的对象数量 可删除
                for (FlameGraphNode nextTierNode : nextTierNodes) {
                    if (nextTierNode.getyHigh() <= thisTierNode.getyHigh()
                            && nextTierNode.getyLow() >= thisTierNode.getyLow()) {
                        children.add(nextTierNode);
                    } else if (nextTierNode.getyHigh() < thisTierNode.getyLow()) {
                        break;
                    }
                }

                if (!children.isEmpty()) {
                    thisTierNode.setChildren(children);
                }
            }

        }
        rootNode.setValue(rootValue);

        String flameGraphJson = mapper.writeValueAsString(rootNode);

        System.out.println("execute time: " + (System.currentTimeMillis() - b));
       // System.out.println("produce object number:" + " " + objectCount);

        return flameGraphJson;
    }

    public static String parseJson(File file) throws IOException {

        long b;
        b = System.currentTimeMillis();
        List<String> metadata = new ArrayList<>();

        if (!file.isFile() || !file.exists()) {
            LOGGER.debug("Fail open file: " + file.getName());
            throw new IOException("can not find file.");
        }

        try (InputStreamReader read = new InputStreamReader(new FileInputStream(file),
                StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(read)) {
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                //LOGGER.debug("read line from file: " + line);
                metadata.add(line);
            }
        }
        LOGGER.debug("metaData size is: " + metadata.size());

        FlameGraphNode rootNode = new FlameGraphNode();
        rootNode.setName("root");

        int rootValue = 0;
        int maxTier = 0;
        //int objectCount = 0; //TODO 记录产生的对象数量 可删除
        List<LinkedList<FlameGraphNode>> nodeChainList = new ArrayList<>();

        for (int j = 0; j < metadata.size(); j++) {
            String line = metadata.get(j);
            int num = Integer.valueOf(line.substring(line.lastIndexOf(" ") + 1));
            line = line.substring(0, line.lastIndexOf(" "));
            String[] elements = line.split("\\|");

            LinkedList<FlameGraphNode> nodeChain = new LinkedList<>();
            nodeChainList.add(nodeChain);
            for (int i = 1; i <= elements.length; i++) {
                FlameGraphNode node =
                        new FlameGraphNode(elements[i - 1],num,metadata.size() - j,metadata.size() - j);
                nodeChain.add(node);


                //objectCount++; //TODO 记录产生的对象数量 可删除
                if (i == 1) {
                    node.setPreName(rootNode.getName());
                } else {
                    node.setPreName(elements[i - 2]);
                }

            }
            if (maxTier < elements.length) {
                maxTier = elements.length;
            }
            rootValue += num;
        }

        List<List<FlameGraphNode>> tierNodesList = new ArrayList<>();
        for (int i = 0; i < maxTier; i++) {
            List<FlameGraphNode> thisList = new ArrayList<>();
            //objectCount++; //TODO 记录产生的对象数量 可删除
            for (LinkedList<FlameGraphNode> nodeChain : nodeChainList) {
                if (nodeChain.size() >= i + 1) {
                    FlameGraphNode node = nodeChain.get(i);
                    thisList.add(node);
                }
            }

            LinkedList<FlameGraphNode> mergeList = new LinkedList<>();
            tierNodesList.add(mergeList);
            // objectCount++; //TODO 记录产生的对象数量 可删除

            for (FlameGraphNode node : thisList) {
                if (mergeList.size() == 0 || !mergeList.getLast().getName().equals(node.getName())) {
                    // mergeList.add(node.deepClone());
                    mergeList.add(node);
                } else {
                    FlameGraphNode last = mergeList.getLast();
                    if (node.getyHigh().equals(last.getyLow() - 1) && node.getPreName().equals(last.getPreName())) {
                        last.setyLow(node.getyLow());
                        last.setValue(last.getValue() + node.getValue());
                    } else {
                        // mergeList.add(node.deepClone());
                        mergeList.add(node);
                    }
                }
            }
        }

        for (int i = 0; i < tierNodesList.size(); i++) {
            List<FlameGraphNode> thisTierNodes = tierNodesList.get(i);
            if (i == 0) {
                rootNode.setChildren(thisTierNodes);
            }

            List<FlameGraphNode> nextTierNodes;
            if (i < tierNodesList.size() - 1) {
                nextTierNodes = tierNodesList.get(i + 1);
            } else {
                break;
            }

            for (FlameGraphNode thisTierNode : thisTierNodes) {
                List<FlameGraphNode> children = new ArrayList<>();
                //objectCount++; //TODO 记录产生的对象数量 可删除
                for (FlameGraphNode nextTierNode : nextTierNodes) {
                    if (nextTierNode.getyHigh() <= thisTierNode.getyHigh()
                            && nextTierNode.getyLow() >= thisTierNode.getyLow()) {
                        children.add(nextTierNode);
                    } else if (nextTierNode.getyHigh() < thisTierNode.getyLow()) {
                        break;
                    }
                }

                if (!children.isEmpty()) {
                    thisTierNode.setChildren(children);
                }
            }

        }
        rootNode.setValue(rootValue);

        String flameGraphJson = mapper.writeValueAsString(rootNode);

        System.out.println("execute time: " + (System.currentTimeMillis() - b));
        // System.out.println("produce object number:" + " " + objectCount);

        return flameGraphJson;
    }

    /**
     * 将火焰图数据解析为多个火焰图节点进行json序列化.
     */
    static class FlameGraphNode implements Serializable {

        private static final long serialVersionUID = 1668513735481340291L;

        /**
         * 子节点,非空时才序列化
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<FlameGraphNode> children;

        /**
         * 节点名称
         */
        private String name;

        /**
         * 节点宽度
         */
        private Integer value;

        /**
         * 仅在解析时使用,不进行序列化
         */
        @JsonIgnore
        private Integer yHigh;

        /**
         * 仅在解析时使用,不进行序列化
         */
        @JsonIgnore
        private Integer yLow;

        /**
         * 仅在解析时使用,不进行序列化
         */
        @JsonIgnore
        private String preName;

        public FlameGraphNode() {}

        public FlameGraphNode(String name, Integer value, Integer yHigh, Integer yLow) {
            this.name = name;
            this.value = value;
            this.yHigh = yHigh;
            this.yLow = yLow;
        }

        public String getPreName() {
            return preName;
        }

        public void setPreName(String preName) {
            this.preName = preName;
        }

        public List<FlameGraphNode> getChildren() {
            return children;
        }

        public void setChildren(List<FlameGraphNode> children) {
            this.children = children;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public Integer getyHigh() {
            return yHigh;
        }

        public void setyHigh(Integer yHigh) {
            this.yHigh = yHigh;
        }

        public Integer getyLow() {
            return yLow;
        }

        public void setyLow(Integer yLow) {
            this.yLow = yLow;
        }

        /**
         * 深拷贝
         */
        public FlameGraphNode deepClone() throws IOException, ClassNotFoundException {
            //将对象写入流中
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(this);
            //从流中取出
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            return (FlameGraphNode)objectInputStream.readObject();

        }
    }
}
