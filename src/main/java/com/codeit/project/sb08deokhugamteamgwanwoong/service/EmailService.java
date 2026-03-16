package com.codeit.project.sb08deokhugamteamgwanwoong.service;

public interface EmailService {
  void sendVerificationCode(String to, String code);
}
