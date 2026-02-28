package com.codeit.project.sb08deokhugamteamgwanwoong.controller.support;

import com.codeit.project.sb08deokhugamteamgwanwoong.config.MDCLoggingInterceptor;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.BookMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.CommentMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.NotificationMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.ReviewMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.UserMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.BookService;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.CommentService;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.NotificationService;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.ReviewService;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
})
@Import(MDCLoggingInterceptor.class)
public abstract class ControllerTestSupport {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected ObjectMapper objectMapper;

  /* Service 부분 */
  @MockitoBean
  protected UserService userService;

  @MockitoBean
  protected CommentService commentService;

  @MockitoBean
  protected NotificationService notificationService;

  @MockitoBean
  protected ReviewService reviewService;

  @MockitoBean
  protected BookService bookService;

  /* Mapper 부분 */
  @MockitoBean
  protected UserMapper userMapper;

  @MockitoBean
  protected CommentMapper commentMapper;

  @MockitoBean
  protected NotificationMapper notificationMapper;

  @MockitoBean
  protected ReviewMapper reviewMapper;

  @MockitoBean
  protected BookMapper bookMapper;
}
