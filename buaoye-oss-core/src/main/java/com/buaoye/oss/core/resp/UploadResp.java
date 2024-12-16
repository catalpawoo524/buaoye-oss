package com.buaoye.oss.core.resp;

import java.io.Serializable;

/**
 * 上传响应结果
 *
 * @author Jayson Wu
 * @since 2024-12-13
 */
public class UploadResp implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件访问地址
     */
    private String url;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 文件密钥
     */
    private String secret;

    public UploadResp() {
    }

    public UploadResp(String url, Long size, String secret) {
        this.url = url;
        this.size = size;
        this.secret = secret;
    }

    public String getUrl() {
        return url;
    }

    public UploadResp setUrl(String url) {
        this.url = url;
        return this;
    }

    public Long getSize() {
        return size;
    }

    public UploadResp setSize(Long size) {
        this.size = size;
        return this;
    }

    public String getSecret() {
        return secret;
    }

    public UploadResp setSecret(String secret) {
        this.secret = secret;
        return this;
    }

}
