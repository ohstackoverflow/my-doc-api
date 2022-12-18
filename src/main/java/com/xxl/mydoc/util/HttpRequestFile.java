package com.xxl.mydoc.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import static com.xxl.mydoc.myconst.MyConst.DOC_HOST;
import static com.xxl.mydoc.myconst.MyConst.PAN_ACCESS_TOKEN;

@Slf4j
public class HttpRequestFile {

    static String get(String url) throws IOException {
        BufferedReader in = null;

        URL realUrl = new URL(url);
        // 打开和URL之间的连接
        URLConnection connection = realUrl.openConnection();
        // 设置通用的请求属性
        //connection.setRequestProperty("accept", "*/*");
        //connection.setRequestProperty("connection", "Keep-Alive");
        //connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        connection.setRequestProperty("User-Agent","pan.baidu.com");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        // 建立实际的连接
        connection.connect();
        // 定义 BufferedReader输入流来读取URL的响应
        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = in.readLine()) != null) {
            sb.append(line);
        }

        in.close();
        return sb.toString();
    }

    private static String RequestPan(String strUrl) {
        try{
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("User-Agent","pan.baidu.com");
            conn.connect();
            OutputStream dataOut = new DataOutputStream(conn.getOutputStream());
            String p = "{}";
            dataOut.write(p.getBytes());
            dataOut.flush();
            dataOut.close();

            StringBuilder result = new StringBuilder();

            BufferedReader reader = null;
            String line = null;
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));// 发送http请求

                // 循环读取流
                while ((line = reader.readLine()) != null) {
                    result.append(line).append(System.getProperty("line.separator"));//
                }
            }

            log.warn( String.format("*** Response Code:%s", conn.getResponseCode()));

            return result.toString();

        }catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static void download(String strUrl, boolean setAgent, HttpServletResponse response) {
        try {
            log.info(strUrl);

            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            if(setAgent) {
                conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            }

            // 设置超时间为3秒
            conn.setConnectTimeout(3 * 1000);
            //获取输入流
            InputStream inputStream = conn.getInputStream();
            //获取输出流
            ServletOutputStream outputStream = response.getOutputStream();
            //每次下载1024位
            byte[] b =new byte[1024];
            int len = -1;
            while((len = inputStream.read(b))!=-1) {
                outputStream.write(b, 0, len);
            }
            inputStream.close();
            outputStream.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    public static void getFile(HttpServletRequest request, HttpServletResponse response, String name, boolean isDownload) {

        String finalUrl = "";

        if(name.indexOf("http") != -1) {   //替换：使用网盘打开，用户自行下载
            try {
                //response.setContentType("text/html;charset=utf-8");
                response.getWriter().append("<script>window.location.href='" + name + "';</script>");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            String folder = "";
            String fileName = name;
            int pLash = name.indexOf("/");
            if(pLash != -1) {
                folder = name.substring(0, pLash);
                fileName = name.substring(pLash+1, name.length());
            }

            String userAgent = request.getHeader("USER-AGENT").toLowerCase();
            log.warn(userAgent);

            try {

                if(userAgent.contains("safari") && !userAgent.contains("chrome")) {
                    //folder = URLEncoder.encode(folder, "utf-8");
                    byte[] bytes = fileName.getBytes("utf-8");
                    String fileShowName = new String(bytes,"ISO-8859-1");
                    response.setHeader("content-disposition",String.format("attachment;filename=\"%s\"",fileShowName));

                } else {
                    folder = URLEncoder.encode(folder, "utf-8");
                    fileName = URLEncoder.encode(fileName, "utf-8");

                    response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
                }

                response.setContentType("applicatoin/octet-stream");

                if(name.endsWith(".zip")) {

                    String strUrl = "http://pan.baidu.com/rest/2.0/xpan/multimedia?method=listall&path=/API共享文档/&access_token=" + PAN_ACCESS_TOKEN + "&web=1&recursion=1&start=0&limit=1000";
                    log.warn(strUrl);
                    String strJson = RequestPan(strUrl);

                    JSONObject retJson = (JSONObject)JSONObject.toJSON(JSON.parse(strJson));
                    JSONArray arrJson = ((JSONArray)retJson.get("list"));
                    for(int i = 0; i <= arrJson.size()-1; i++) {
                        if( name.endsWith( ((JSONObject)arrJson.get(i)).get("server_filename").toString() )) {
                            String fsId = ((JSONObject)arrJson.get(i)).get("fs_id").toString();
                            String strUrl2 = String.format("http://pan.baidu.com/rest/2.0/xpan/multimedia?method=filemetas&access_token=" + PAN_ACCESS_TOKEN + "&fsids=[%s]&thumb=1&dlink=1&extra=1",fsId);
                            log.warn(strUrl2);
                            String strJson2 = RequestPan(strUrl2);
                            String dlink =   ( (JSONObject) ( (JSONArray) ( (JSONObject)JSONObject.toJSON(JSON.parse(strJson2)) ).get("list") ).get(0) ).get("dlink").toString() ;
                            finalUrl = dlink + "&access_token=" + PAN_ACCESS_TOKEN;
                            log.warn(finalUrl);
                            break;
                        }
                    }


                    response.setContentType("application/x-download");
                    response.setHeader("Cache-Control", "no-cache");

                    download(finalUrl, true, response);

                } else {

                    //log.info(URLEncoder.encode(fileName, "utf-8"));
                    finalUrl = DOC_HOST + folder + "/" + fileName;

                    download(finalUrl, false, response);

                }



            } catch (IOException ex) {
                ex.printStackTrace();
            }


        }


    }


}
