package ru.practicum.main.comment.service;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.interaction.common.ConflictException;
import ru.practicum.interaction.common.NotFoundException;
import ru.practicum.interaction.dto.comment.CommentDto;
import ru.practicum.interaction.dto.comment.CommentDtoPublic;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventStateDto;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.feign.client.EventServiceClient;
import ru.practicum.interaction.feign.client.UserServiceClient;
import ru.practicum.main.comment.mapper.CommentMapper;
import ru.practicum.main.comment.model.Comment;
import ru.practicum.main.comment.repository.CommentRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final EventServiceClient eventServiceClient;
    private final UserServiceClient userServiceClient;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;

    @Override
    public CommentDto addCommentToEvent(Long authorId, Long eventId, CommentDto commentDto) {
        Comment comment = commentMapper.toComment(commentDto);
        EventFullDto event = eventServiceClient.getById(eventId);
        userServiceClient.getUser(authorId);
        if (event.getState().equals(EventStateDto.PUBLISHED)) {
            comment.setAuthorId(authorId);
            comment.setEventId(eventId);
            comment.setCreate(LocalDateTime.now());
            log.info("Add new comment for event id = {} and user with ID = {}.", eventId, authorId);
            return commentMapper.toCommentDto(commentRepository.saveAndFlush(comment));
        } else {
            String message = MessageFormat.format("Event {0} has not published yet.", eventId);
            log.error(message);
            throw new ConflictException(message);
        }
    }

    @Override
    public CommentDto getCommentByUser(Long authorId, Long commentId) {
        log.info("Get comment with ID = {} for user with ID = {}.", commentId, authorId);
        return commentMapper.toCommentDto(getCommentById(commentId));
    }

    @Override
    public List<CommentDto> getAllCommentsByEvent(Long eventId) {
        EventFullDto event = eventServiceClient.getById(eventId);
        if (event.getState().equals(EventStateDto.PUBLISHED)) {
            List<Comment> comments = commentRepository.findAllByEventIdOrderByEventId(eventId);
            log.info("Get comment for event ID = {}.", eventId);
            return comments.stream().map(commentMapper::toCommentDto).collect(Collectors.toList());
        } else {
            String message = MessageFormat.format("Event {0} has not published yet.", eventId);
            log.error(message);
            throw new ConflictException(message);
        }
    }

    @Override
    public CommentDto updateCommentByUser(Long authorId, Long commentId, CommentDto commentDto) {
        Comment comment = getCommentById(commentId);
        UserDto author = userServiceClient.getUser(authorId);
        if (comment.getAuthorId().equals(author.getId())) {
            comment.setText(commentDto.getText());
            comment.setCreate(LocalDateTime.now());
            log.info("Update comment with ID {}.", commentId);
            return commentMapper.toCommentDto(commentRepository.save(comment));
        } else {
            String message = MessageFormat.format("The user ID {0} not the author of comment ID {1}.", authorId,
                    commentId);
            log.error(message);
            throw new ConflictException(message);
        }
    }

    @Override
    public void deleteCommentByUser(Long authorId, Long commentId) {
        Comment comment = getCommentById(commentId);
        if (comment.getAuthorId().equals(authorId)) {
            log.info("Delete comment with ID = {} by user ID {}.", commentId, authorId);
            commentRepository.deleteById(getCommentById(commentId).getId());
        } else {
            throw new ConflictException("The user is not the author of the comment.");
        }
    }

    @Override
    public CommentDto updateCommentByAdmin(Long commentId, CommentDto commentDto) {
        Comment comment = getCommentById(commentId);
        comment.setText(commentDto.getText());
        comment.setCreate(LocalDateTime.now());
        log.info("Update comment with ID= {} by admin.", commentId);
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        log.info("Delete comment with ID = {} by admin.", commentId);
        commentRepository.deleteById(getCommentById(commentId).getId());
    }

    @Override
    public List<CommentDtoPublic> getAllCommentsByEventPublic(Long eventId) {
        EventFullDto event = eventServiceClient.getById(eventId);
        if (event.getState().equals(EventStateDto.PUBLISHED)) {
            List<Comment> comments = commentRepository.findAllByEventIdOrderByEventId(eventId);
            log.info("Get public comment for event with ID = {}.", eventId);
            return comments.stream().map(commentMapper::toCommentDtoPublic).collect(Collectors.toList());
        } else {
            String message = MessageFormat.format("The event {0} has not been published yet. Commenting is not " +
                                                  "available.", eventId);
            log.error(message);
            throw new ConflictException(message);
        }
    }

    public Comment getCommentById(Long commentId) {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        if (commentOptional.isPresent()) {
            return commentOptional.get();
        }
        throw new NotFoundException("Comment with ID = " + commentId + " was not found.");
    }
}
