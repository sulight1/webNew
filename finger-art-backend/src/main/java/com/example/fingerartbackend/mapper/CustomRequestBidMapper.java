package com.example.fingerartbackend.mapper;

import com.example.fingerartbackend.entity.CustomRequestBid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomRequestBidMapper extends JpaRepository<CustomRequestBid, Long> {

    List<CustomRequestBid> findByRequestIdOrderByCreateTimeDesc(Long requestId);

    Optional<CustomRequestBid> findByRequestIdAndArtisanId(Long requestId, Long artisanId);

    List<CustomRequestBid> findByArtisanIdOrderByCreateTimeDesc(Long artisanId);

    long countByRequestIdAndStatus(Long requestId, String status);
}
