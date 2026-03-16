package com.codeit.project.sb08deokhugamteamgwanwoong.service;

public interface EmailService {
  void sendVerificationCode(String toEmail, String code);
}
