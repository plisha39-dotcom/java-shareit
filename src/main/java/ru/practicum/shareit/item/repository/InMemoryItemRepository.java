package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private long nextId = 1;

    @Override
    public List<Item> findAll() {
        return List.copyOf(items.values());
    }

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(nextId);
            nextId++;
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }
}
