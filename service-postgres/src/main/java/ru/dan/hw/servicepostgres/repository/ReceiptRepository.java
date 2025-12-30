package ru.dan.hw.servicepostgres.repository;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.dan.hw.servicepostgres.entity.Receipt;

import java.util.List;

public interface ReceiptRepository extends JpaRepository<Receipt, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select r
        from Receipt r
        where r.sentToBroker = false
        order by r.id
    """)
    List<Receipt> findUnsentForUpdate(Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
        update Receipt r
           set r.sentToBroker = true
         where r.id = :id
           and r.sentToBroker = false
    """)
    int markAsSent(@Param("id") Integer id);
}
