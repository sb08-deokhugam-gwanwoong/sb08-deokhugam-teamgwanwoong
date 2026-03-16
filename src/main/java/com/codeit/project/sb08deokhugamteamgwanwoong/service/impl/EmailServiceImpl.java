package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.UserErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender mailSender;

  @Override
  public void sendVerificationCode(String toEmail, String code) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(toEmail);
    message.setSubject("[덕후감] 비밀번호 찾기 인증번호 안내");
    message.setText(createEmailContent(code));

    try {
      mailSender.send(message);
    } catch (MailException e) {
      log.error("[메일 발송 실패] email: {}, error: {}", toEmail, e.getMessage());
      throw new BusinessException(UserErrorCode.EMAIL_SEND_FAILED);
    }
  }

  private String createEmailContent(String code) {
    return "안녕하세요, 관웅입니다.\n\n" +
        "비밀번호 재설정을 위한 인증번호는 다음과 같습니다.\n" +
        "인증번호: [" + code + "]\n\n" +
        "인증번호의 유효 시간은 3분입니다.\n" +
        "감사합니다.";
  }
}
