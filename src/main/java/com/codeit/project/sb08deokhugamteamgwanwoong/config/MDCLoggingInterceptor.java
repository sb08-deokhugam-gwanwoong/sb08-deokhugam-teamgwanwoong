package com.codeit.project.sb08deokhugamteamgwanwoong.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/* 요청이 들어올 때 MDC에 정보를 넣고, 나갈 때 지우는 역할 */
@Component
public class MDCLoggingInterceptor implements HandlerInterceptor {

  private static final String REQUEST_ID_HEADER = "Deokhugam-Request-ID";
  private static final String MDC_REQUEST_ID = "request_id";
  private static final String MDC_METHOD = "method";
  private static final String MDC_URI = "uri";
  private static final String MDC_CLIENT_IP = "client_ip";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {

    String requestId = UUID.randomUUID().toString().substring(0, 8); // 8자리만 사용

    // MDC에 값 저장
    MDC.put(MDC_REQUEST_ID, requestId);
    MDC.put(MDC_METHOD, request.getMethod());
    MDC.put(MDC_URI, request.getRequestURI());
    MDC.put(MDC_CLIENT_IP, getClientIp(request));

    // 응답 헤더에 요청 ID 추가 (클라이언트 추적 가능하게)
    response.setHeader(REQUEST_ID_HEADER, requestId);

    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) {

    // 요청 처리가 끝나면 MDC 정리 (ThreadLocal이므로 필수로 해줘야 함)
    MDC.clear();
  }

  // 클라이언트 IP 주소 추출 메서드
  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_CLIENT_IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }

    // 로컬호스트 IPv6 주소를 IPv4로 변환하여 가독성 높임
    if ("0:0:0:0:0:0:0:1".equals(ip)) {
      return "127.0.0.1";
    }

    return ip;
  }
}
