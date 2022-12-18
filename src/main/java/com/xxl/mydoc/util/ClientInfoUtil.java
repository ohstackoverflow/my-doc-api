package com.xxl.mydoc.util;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ClientInfoUtil {
    /**
     * 获取客户端IP，支持反向代理，如nginx，但不支持正向代理，比如客户端浏览器自己使用代理工具
     * @param request
     * @return 客户端IP
     */
    public static String getClientIP(HttpServletRequest request)
    {
        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("Proxy-Client-IP");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
            ip = request.getRemoteAddr();
        return ip;
    }

    public static String getLocalMac(HttpServletRequest request)  {
        StringBuilder sb = new StringBuilder();
        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            //System.out.println("IP address : " + ip.getHostAddress());
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            System.out.print("MAC address : ");

            if(mac == null) {
                return getClientIP(request);
            }

            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
