package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentSearchCondition;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Comment;
import java.util.List;

public interface CommentRepositoryCustom {

  List<Comment> findAllByCursor(CommentSearchCondition condition);

}
