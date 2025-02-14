package com.buaoye.oss.core.req;

import com.amazonaws.util.IOUtils;
import com.buaoye.oss.common.exception.BuaoyeException;
import com.buaoye.oss.common.exception.ErrorCodeConstant;
import org.springframework.http.HttpMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 上传文件请求抽象类
 *
 * @author Jayson Wu
 * @since 2024-12-13
 */
public class UploadReq {

    /**
     * 文件对象
     */
    protected final Object fileObj;

    public UploadReq(Object fileObj) {
        if (fileObj == null) {
            throw new BuaoyeException(ErrorCodeConstant.OSS_FILE_UPLOAD_NULL_EXCEPTION);
        }
        this.fileObj = fileObj;
    }

    /**
     * 获取文件输入流，重写该方法以支持更多文件类型
     *
     * @return 文件输入流
     * @throws IOException IO异常
     */
    public ByteArrayInputStream getInputStream() throws IOException {
        if (fileObj instanceof ByteArrayInputStream) {
            ByteArrayInputStream inputStream = (ByteArrayInputStream) fileObj;
            inputStream.reset();
            return inputStream;
        } else if (fileObj instanceof MultipartFile) {
            MultipartFile multipartFile = (MultipartFile) fileObj;
            // 多媒体文件，使用流式复制而不是一次性读取文件
            return new ByteArrayInputStream(IOUtils.toByteArray(multipartFile.getInputStream()));
        } else if (fileObj instanceof URL) {
            // 从对应的 URL 下载
            URL url = (URL) fileObj;
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(HttpMethod.GET.name());
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(60_000);
            connection.connect();
            return new ByteArrayInputStream(IOUtils.toByteArray(connection.getInputStream()));
        }
        throw new BuaoyeException(ErrorCodeConstant.OSS_FILE_UPLOAD_UNKNOWN_EXCEPTION);
    }

}
