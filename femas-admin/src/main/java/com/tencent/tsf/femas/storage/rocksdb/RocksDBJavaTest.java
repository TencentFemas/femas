package com.tencent.tsf.femas.storage.rocksdb;

import com.tencent.tsf.femas.storage.StorageResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/4/19 15:30
 * @Version 1.0
 */
public class RocksDBJavaTest {


    private static final String dbPath = System.getProperty("user.home").concat("/rocksdb/java/data/");

    static {
        RocksDB.loadLibrary();
    }

    RocksDB rocksDB;

    public static void main(String[] args) throws Exception {
        StringRawKVStoreManager rocksDbKvStore = new StringRawKVStoreManager();
        StorageResult<List<Map<String, String>>> sr = rocksDbKvStore.scanAll();

        // 清空rocksdb
        List<Map<String, String>> list = sr.getData();
//        for (Map<String, String> map: list) {
//            for (String key: map.keySet()) {
//                rocksDbKvStore.delete(key);
//            }
//        }

//        String key = "Hello/9999";
//        String key1 = "Hello/123";
//        String key2 = "Hello/qwe";
//        String key3 = "Hello/iiiiiiiiiii";
//        String value = "leoooooooooooo";
////        StorageResult result = rocksDbKvStore.get(key);
//        StorageResult result1 = rocksDbKvStore.put(key, value);
//        StorageResult result2 = rocksDbKvStore.put(key1, value);
//        StorageResult result3 = rocksDbKvStore.put(key2, value);
//        StorageResult result4 = rocksDbKvStore.get(key1);
//        StorageResult result45 = rocksDbKvStore.scanPrefix("Hello");
//        StorageResult result46 = rocksDbKvStore.containsKey("Hello");
//        StorageResult result47 = rocksDbKvStore.containsKey(key);
////        System.out.println(result.getData());
//        System.out.println(result2.getData());
//        test.testDefaultColumnFamily();
//        test.testCertainColumnFamily();
    }

    //  RocksDB.DEFAULT_COLUMN_FAMILY
    public void testDefaultColumnFamily() throws RocksDBException, IOException {
        Options options = new Options();
        options.setCreateIfMissing(true);
        System.out.println(dbPath);
        // 文件不存在，则先创建文件
        if (!Files.isSymbolicLink(Paths.get(dbPath))) {
            Files.createDirectories(Paths.get(dbPath));
        }
        rocksDB = RocksDB.open(options, dbPath);

        /**
         * 简单key-value
         */
        byte[] key = "Hello".getBytes();
        byte[] value = "World".getBytes();
        byte[] value2 = "World_leooooooooo".getBytes();
        rocksDB.put(key, value);
        rocksDB.put(key, value2);

        byte[] getValue = rocksDB.get(key);
        System.out.println(new String(getValue));

        /**
         * 通过List做主键查询
         */
        rocksDB.put("SecondKey".getBytes(), "SecondValue".getBytes());

        List<byte[]> keys = new ArrayList<>();
        keys.add(key);
        keys.add("SecondKey".getBytes());

        Map<byte[], byte[]> valueMap = rocksDB.multiGet(keys);
        for (Map.Entry<byte[], byte[]> entry : valueMap.entrySet()) {
            System.out.println(new String(entry.getKey()) + ":" + new String(entry.getValue()));
        }

        /**
         *  打印全部[key - value]
         */
        RocksIterator iter = rocksDB.newIterator();
        for (iter.seekToFirst(); iter.isValid(); iter.next()) {
            System.out.println("iter key:" + new String(iter.key()) + ", iter value:" + new String(iter.value()));
        }

        /**
         *  删除一个key
         */
        rocksDB.delete(key);
        System.out.println("after remove key:" + new String(key));

        iter = rocksDB.newIterator();
        for (iter.seekToFirst(); iter.isValid(); iter.next()) {
            System.out.println("iter key:" + new String(iter.key()) + ", iter value:" + new String(iter.value()));
        }
    }

    public void testCertainColumnFamily() throws RocksDBException {
        String table = "CertainColumnFamilyTest";
        String key = "certainKey";
        String value = "certainValue";

        List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
        Options options = new Options();
        options.setCreateIfMissing(true);

        List<byte[]> cfs = RocksDB.listColumnFamilies(options, dbPath);
        if (cfs.size() > 0) {
            for (byte[] cf : cfs) {
                columnFamilyDescriptors.add(new ColumnFamilyDescriptor(cf, new ColumnFamilyOptions()));
            }
        } else {
            columnFamilyDescriptors
                    .add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions()));
        }

        List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
        DBOptions dbOptions = new DBOptions();
        dbOptions.setCreateIfMissing(true);

        rocksDB = RocksDB.open(dbOptions, dbPath, columnFamilyDescriptors, columnFamilyHandles);
        for (int i = 0; i < columnFamilyDescriptors.size(); i++) {
            if (new String(columnFamilyDescriptors.get(i).columnFamilyName()).equals(table)) {
                rocksDB.dropColumnFamily(columnFamilyHandles.get(i));
            }
        }

        ColumnFamilyHandle columnFamilyHandle = rocksDB
                .createColumnFamily(new ColumnFamilyDescriptor(table.getBytes(), new ColumnFamilyOptions()));
        rocksDB.put(columnFamilyHandle, key.getBytes(), value.getBytes());

        byte[] getValue = rocksDB.get(columnFamilyHandle, key.getBytes());
        System.out.println("get Value : " + new String(getValue));

        rocksDB.put(columnFamilyHandle, "SecondKey".getBytes(), "SecondValue".getBytes());

        List<byte[]> keys = new ArrayList<byte[]>();
        keys.add(key.getBytes());
        keys.add("SecondKey".getBytes());

        List<ColumnFamilyHandle> handleList = new ArrayList<>();
        handleList.add(columnFamilyHandle);
        handleList.add(columnFamilyHandle);

        Map<byte[], byte[]> multiGet = rocksDB.multiGet(handleList, keys);
        for (Map.Entry<byte[], byte[]> entry : multiGet.entrySet()) {
            System.out.println(new String(entry.getKey()) + "--" + new String(entry.getValue()));
        }

        rocksDB.delete(columnFamilyHandle, key.getBytes());

        RocksIterator iter = rocksDB.newIterator(columnFamilyHandle);
        for (iter.seekToFirst(); iter.isValid(); iter.next()) {
            System.out.println(new String(iter.key()) + ":" + new String(iter.value()));
        }
        options.close();
    }

}

