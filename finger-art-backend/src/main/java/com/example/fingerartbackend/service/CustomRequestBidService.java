package com.example.fingerartbackend.service;

import com.example.fingerartbackend.dto.CustomRequestBidView;
import com.example.fingerartbackend.dto.SelectBidResult;
import com.example.fingerartbackend.entity.CustomRequestBid;

import java.util.List;

public interface CustomRequestBidService {

    CustomRequestBid submitBid(Long requestId, Long artisanId, String message);

    List<CustomRequestBidView> listBidsForRequest(Long requestId, Long viewerUserId);

    List<Long> listBidRequestIdsByArtisan(Long artisanId);

    long countPendingBids(Long requestId);

    SelectBidResult selectBid(Long requestId, Long buyerId, Long bidId);
}
