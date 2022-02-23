package com.tencent.tsf.femas.common.util.id;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author leoziltong
 * @Description //TODO
 * @Date: 2021/3/31 15:13
 * @Version 1.0
 */
public class HostNameKeyGenerator implements KeyGenerator {

    /**
     * 根据机器名最后的数字编号获取工作进程Id.如果线上机器命名有统一规范,建议使用此种方式.
     * 例如机器的HostName为:tencent-csig-tsf-dev-01(公司名-部门名-服务名-环境名-编号)
     * ,会截取HostName最后的编号01作为workerId.
     **/
    private static Long workerId;

    private static SnowflakeIdWorker worker;

    static {
        InetAddress address;
        try {
            address = InetAddress.getLocalHost();
        } catch (final UnknownHostException e) {
            throw new IllegalStateException("Cannot get LocalHost InetAddress, please check your network!");
        }
        // 先得到服务器的hostname，例如JTCRTVDRA44，linux上可通过命令"cat /proc/sys/kernel/hostname"查看；
        String hostName = address.getHostName();
        try {
            // 计算workerId的方式：
            // 第一步hostName.replaceAll("\\d+$", "")，即去掉hostname后纯数字部分，例如JTCRTVDRA44去掉后就是JTCRTVDRA
            // 第二步hostName.replace(第一步的结果, "")，即将原hostname的非数字部分去掉，得到纯数字部分，就是workerId
            workerId = Long.valueOf(hostName.replace(hostName.replaceAll("\\d+$", ""), ""));
            worker = new SnowflakeIdWorker(workerId);
        } catch (final NumberFormatException e) {
            // 如果根据hostname截取不到数字，那么抛出异常
            throw new IllegalArgumentException(
                    String.format("Wrong hostname:%s, hostname must be end with number!", hostName));
        }
    }

    @Override
    public long generate() {
        return worker.nextId();
    }
}
