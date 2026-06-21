package com.example.fingerartbackend.service;

import com.example.fingerartbackend.entity.CustomOrder;
import com.example.fingerartbackend.entity.EscrowTransaction;
import com.example.fingerartbackend.entity.OrderMilestone;

import java.util.List;
import java.util.Map;

public interface OrderService {
    CustomOrder createOrder(CustomOrder order);
    List<CustomOrder> getArtisanOrders(Long artisanId);
    List<CustomOrder> getBuyerOrders(Long buyerId);
    List<CustomOrder> getAllOrders(String status);
    CustomOrder getOrder(Long id);
    CustomOrder confirmOrder(Long orderId, Long artisanId);
    CustomOrder payDeposit(Long orderId, Long buyerId, String paymentChannel);
    CustomOrder payBalance(Long orderId, Long buyerId);
    CustomOrder confirmReceipt(Long orderId, Long buyerId);
    CustomOrder updateStatus(Long orderId, String status, Long operatorId, String operatorName);
    CustomOrder shipOrder(Long orderId, Long artisanId, String shippingCompany, String trackingNumber, String operatorName);
    CustomOrder addMilestone(Long orderId, Map<String, String> payload, Long operatorId, String operatorName);
    List<OrderMilestone> getMilestones(Long orderId);
    List<EscrowTransaction> getEscrowTransactions(Long orderId);
    CustomOrder openDispute(Long orderId, Long userId, String reason);
    CustomOrder resolveDispute(Long orderId, boolean releaseToArtisan);
    CustomOrder requestCancel(Long orderId, Long buyerId, String reason);
    CustomOrder approveCancel(Long orderId, Long artisanId);
    CustomOrder rejectCancel(Long orderId, Long artisanId, String reason);
    void deleteOrder(Long id);
}
