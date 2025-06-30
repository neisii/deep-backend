package org.example.backendproject.threadlocal;

public class TraceIdHolder {
    // TraceId: 개별 요청의 고유 식별자(ID)

    private static final ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public static void set(String traceId) {
        threadLocal.set(traceId);
    }

    public static String get() {
        return threadLocal.get();
    }

    // 반드시 clear 해야 함.
    public static void clear() {
        threadLocal.remove();
    }

}
