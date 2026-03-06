package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;

public interface NotificationService {
  void createNotification(User user, Review review, String message);
}
