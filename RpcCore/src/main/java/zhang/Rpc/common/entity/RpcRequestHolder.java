package zhang.Rpc.common.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 请求连接
 */
public class RpcRequestHolder {

    // 请求id
    public final static AtomicLong REQUEST_ID_GEN = new AtomicLong(0);

    // 绑定请求
    public static final Map<Long, RpcFuture<RpcResponse>> REQUEST_MAP = new ConcurrentHashMap<>();
}
