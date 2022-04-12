package com.tencent.tsf.femas.util;

import java.util.Comparator;
import java.util.List;
import org.springframework.util.CollectionUtils;

public class PageUtil {

    public static <T> List<T> pageList(List<T> list, Integer pageNum, Integer pageSize) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        list.sort(new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return o1.hashCode() - o2.hashCode();
            }
        });
        Integer count = list.size();
        Integer pageCount = 0;
        if (count % pageSize == 0) {
            pageCount = count / pageSize;
        } else {
            pageCount = count / pageSize + 1;
        }
        int fromIndex = 0;
        int toIndex = 0;
        if (pageNum > pageCount) {
            pageNum = pageCount;
        }
        if (!pageNum.equals(pageCount)) {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = fromIndex + pageSize;
        } else {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = count;
        }
        List<T> pageList = list.subList(fromIndex, toIndex);
        return pageList;
    }

    public static <T> List<T> pageList(List<T> list, Integer pageNum, Integer pageSize, boolean flag) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        if (flag) {
            list.sort(new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    return o1.hashCode() - o2.hashCode();
                }
            });
        }
        Integer count = list.size();
        Integer pageCount = 0;
        if (count % pageSize == 0) {
            pageCount = count / pageSize;
        } else {
            pageCount = count / pageSize + 1;
        }
        int fromIndex = 0;
        int toIndex = 0;
        if (pageNum > pageCount) {
            pageNum = pageCount;
        }
        if (!pageNum.equals(pageCount)) {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = fromIndex + pageSize;
        } else {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = count;
        }
        List<T> pageList = list.subList(fromIndex, toIndex);
        return pageList;
    }

    public static <T> List<T> pageList(List<T> list, Integer pageNum, Integer pageSize, Comparator<T> comparator) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        list.sort(comparator);
        Integer count = list.size();
        Integer pageCount = 0;
        if (count % pageSize == 0) {
            pageCount = count / pageSize;
        } else {
            pageCount = count / pageSize + 1;
        }
        int fromIndex = 0;
        int toIndex = 0;
        if (pageNum > pageCount) {
            pageNum = pageCount;
        }
        if (!pageNum.equals(pageCount)) {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = fromIndex + pageSize;
        } else {
            fromIndex = (pageNum - 1) * pageSize;
            toIndex = count;
        }
        List<T> pageList = list.subList(fromIndex, toIndex);
        return pageList;
    }

}
