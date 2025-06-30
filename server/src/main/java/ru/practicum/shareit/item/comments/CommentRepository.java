package ru.practicum.shareit.item.comments;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long> {
    List<Comment> findByItemIdInOrderByCreated(Collection<Long> itemId);
}
