package com.buaoye.oss.common.exception;

/**
 * 错误码常量类
 *
 * @author Jayson Wu
 */
public class ErrorCodeConstant {

    // ======================== 线程池异常（1_001_001_xxx） ========================
    public static final BayErrorCode THREAD_POOL_DISTRIBUTION_EXCEPTION = BayErrorCode.create(1_001_001_001, "线程池任务分发异常");

    // ======================== 测试异常（1_002_001_xxx） ========================
    public static final BayErrorCode TEST_DOWNLOAD_EXCEPTION = BayErrorCode.create(1_002_001_001, "下载文件测试异常");
    public static final BayErrorCode TEST_GET_BUCKET_EXCEPTION = BayErrorCode.create(1_002_001_002, "桶获取失败");
    public static final BayErrorCode TEST_CLIENT_REUSE_EXCEPTION = BayErrorCode.create(1_002_001_003, "客户端复用测试失败");
    public static final BayErrorCode TEST_CACHE_FILE_EXCEPTION = BayErrorCode.create(1_002_001_004, "缓存文件测试异常");

    // ======================== 文件缓存异常（2_001_001_xxx） ========================
    public static final BayErrorCode FILE_CACHE_LOAD_EXCEPTION = BayErrorCode.create(2_001_001_001, "载入数据失败");
    public static final BayErrorCode FILE_CACHE_LOCK_EXCEPTION = BayErrorCode.create(2_001_001_002, "读取文件流失败");
    public static final BayErrorCode FILE_CACHE_CREATE_EXCEPTION = BayErrorCode.create(2_001_001_003, "创建临时文件异常");

    // ======================== 客户端异常（2_002_001_xxx） ========================
    public static final BayErrorCode CLIENT_CREATE_EXCEPTION = BayErrorCode.create(2_002_001_001, "客户端创建失败");

    // ======================== OSS 操作异常（2_003_001_xxx） ========================
    public static final BayErrorCode OSS_BUCKET_NOT_EXIST_EXCEPTION = BayErrorCode.create(2_003_001_001, "桶不存在或已删除");
    public static final BayErrorCode OSS_FILE_UPLOAD_EXCEPTION = BayErrorCode.create(2_003_001_002, "上传文件失败");
    public static final BayErrorCode OSS_FILE_DOWNLOAD_EXCEPTION = BayErrorCode.create(2_003_001_003, "下载文件失败");
    public static final BayErrorCode OSS_FILE_UPLOAD_NULL_EXCEPTION = BayErrorCode.create(2_003_001_004, "文件对象为空");
    public static final BayErrorCode OSS_FILE_UPLOAD_UNKNOWN_EXCEPTION = BayErrorCode.create(2_003_001_005, "无法解析的文件类型");

}
