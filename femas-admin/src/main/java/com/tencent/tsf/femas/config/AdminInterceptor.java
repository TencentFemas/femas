package com.tencent.tsf.femas.config;

import com.tencent.tsf.femas.common.serialize.JSONSerializer;
import com.tencent.tsf.femas.common.util.Result;
import com.tencent.tsf.femas.util.AESUtils;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;


@Slf4j
public class AdminInterceptor extends HandlerInterceptorAdapter {


    /**
     * 在登录后对请求做一些预处理，token的判断
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String token = request.getHeader("token");
        // 判断token是否非法
        if (token == null || token == "") {
            returnJson(response, JSONSerializer.serializeStr(Result.create(Result.UNAUTHORIZED, "请登录", null)));
            return false;
        }
        // 判断token是否非法
        if (AESUtils.decrypt(token) == null) {
            returnJson(response, JSONSerializer.serializeStr(Result.create(Result.UNAUTHORIZED, "请登录", null)));
            return false;
        }
        return super.preHandle(request, response, handler);
    }

//    public static void main(String[] args) {
//        System.out.println(  JSONSerializer.serializeStr(Result.create(Result.UNAUTHORIZED,"token不存在",null)));
//    }

//    public static void main(String[] args) {
//        InfoModel infoModel = new InfoModel();
//        infoModel.setId(1);
//        infoModel.setSite("hfh");
//        TokenConfig tokenConfig = new TokenConfig();
//        System.out.println(tokenConfig.getToken(JSON.toJSONString(infoModel)));
//        System.out.println(tokenConfig.getToken(JSON.toJSONString(infoModel)));
//    }

    /**
     * 输出HTML
     *
     * @param response
     * @param json
     * @throws Exception
     */
    private void returnJson(HttpServletResponse response, String json) throws Exception {
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        try {
            writer = response.getWriter();
            writer.print(json);

        } catch (IOException e) {
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }
}
