package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBooker_IdOrderByStartDesc(Long bookerId);

    List<Booking> findByBooker_IdAndStatusOrderByStartDesc(Long userId, Status status);

    List<Booking> findByBooker_IdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime now);

    List<Booking> findByBooker_IdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime now);

    List<Booking> findByBooker_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Long bookerId, LocalDateTime now1, LocalDateTime now2);

    List<Booking> findByItem_Owner_IdOrderByStartDesc(Long ownerId);

    List<Booking> findByItem_Owner_IdAndStatusOrderByStartDesc(Long ownerId, Status status);

    List<Booking> findByItem_Owner_IdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime now);

    List<Booking> findByItem_Owner_IdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime now);

    List<Booking> findByItem_Owner_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Long ownerId, LocalDateTime now, LocalDateTime end);

    boolean existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
            Long itemId, Long authorId, Status status, LocalDateTime now);

    List<Booking> findByItem_IdIn(List<Long> itemIds);

    @Query(value = """
                        SELECT *
            FROM (
                SELECT id, item_id, booker_id, start_date, end_date, status,
                ROW_NUMBER() OVER (
                PARTITION BY item_id
                ORDER BY end_date DESC
            ) AS rn
            FROM bookings
            WHERE status = 'APPROVED'
              AND end_date < NOW()
              AND item_id IN (:itemIds)
            ) AS ranked_bookings
            WHERE rn = 1
            """,  nativeQuery = true)
    List<Booking> findByLastBooking(@Param("itemIds") List<Long> itemIds);

    @Query(value = """
                        SELECT *
            FROM (
                SELECT id, item_id, booker_id, start_date, end_date, status,
                ROW_NUMBER() OVER (
                PARTITION BY item_id
                ORDER BY start_date ASC
            ) AS rn
            FROM bookings
            WHERE status = 'APPROVED'
              AND start_date > NOW()
              AND item_id IN (:itemIds)
            ) AS ranked_bookings
            WHERE rn = 1
            """,  nativeQuery = true)
    List<Booking> findByNextBooking(@Param("itemIds") List<Long> itemIds);

    boolean existsByBooker_Id(Long bookerId);
}
