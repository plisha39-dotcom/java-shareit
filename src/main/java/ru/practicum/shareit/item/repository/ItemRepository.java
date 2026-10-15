package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwner_Id(Long userId);

    @Query("""
    SELECT i
    FROM Item i
    WHERE i.available = true AND
        (LOWER(i.name) LIKE CONCAT('%', LOWER(:text) ,'%') OR LOWER(i.description) LIKE CONCAT('%', LOWER(:text) ,'%'))
    """)
    List<Item> searchByText(@Param("text") String text);
}
