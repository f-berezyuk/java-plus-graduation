package ru.practicum.main.comment.mapper;

import java.time.format.DateTimeFormatter;

import ru.practicum.interaction.dto.comment.CommentDto;
import ru.practicum.main.comment.model.Comment;

public class MyCommentMapper {
    public static CommentDto toCommentDto(Comment comment, String authorName) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .eventId(String.valueOf(comment.getEventId()))
                .authorName(authorName)
                .create(comment.getCreate().format(dateTimeFormatter))
                .build();
    }

}
