package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.dto.CustomRequestBidView;
import com.example.fingerartbackend.dto.SelectBidResult;
import com.example.fingerartbackend.entity.CustomOrder;
import com.example.fingerartbackend.entity.CustomRequest;
import com.example.fingerartbackend.entity.CustomRequestBid;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.mapper.CustomRequestBidMapper;
import com.example.fingerartbackend.mapper.CustomRequestMapper;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.service.CustomRequestBidService;
import com.example.fingerartbackend.service.NotificationService;
import com.example.fingerartbackend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomRequestBidServiceImpl implements CustomRequestBidService {

    @Autowired
    private CustomRequestBidMapper bidMapper;

    @Autowired
    private CustomRequestMapper requestMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public CustomRequestBid submitBid(Long requestId, Long artisanId, String message) {
        CustomRequest request = requestMapper.findById(requestId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));
        if (!"OPEN".equals(request.getStatus())) {
            throw new RuntimeException("该需求已结束招募");
        }
        User artisan = userMapper.findById(artisanId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (!"ARTISAN".equals(artisan.getRole())) {
            throw new RuntimeException("仅手作达人可揭榜");
        }
        if (request.getBuyer() != null && artisanId.equals(request.getBuyer().getId())) {
            throw new RuntimeException("不能揭榜自己的需求");
        }
        if (bidMapper.findByRequestIdAndArtisanId(requestId, artisanId).isPresent()) {
            throw new RuntimeException("你已经揭榜过该需求");
        }

        CustomRequestBid bid = new CustomRequestBid();
        bid.setRequestId(requestId);
        bid.setArtisanId(artisanId);
        bid.setMessage(message != null && !message.isBlank()
                ? message.trim()
                : "你好，我想接这个定制需求，期待与您进一步沟通。");
        bid.setStatus("PENDING");
        CustomRequestBid saved = bidMapper.save(bid);

        if (request.getBuyer() != null) {
            notificationService.notify(
                    request.getBuyer().getId(),
                    "REQUEST_BID",
                    "有人揭榜了",
                    artisan.getUsername() + " 对「" + request.getTitle() + "」发起揭榜，请前往查看并选择合作对象",
                    "/account?menu=my-requests");
        }
        return saved;
    }

    @Override
    public List<CustomRequestBidView> listBidsForRequest(Long requestId, Long viewerUserId) {
        CustomRequest request = requestMapper.findById(requestId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));
        if (request.getBuyer() == null || !request.getBuyer().getId().equals(viewerUserId)) {
            throw new RuntimeException("仅需求发起人可查看揭榜列表");
        }
        return bidMapper.findByRequestIdOrderByCreateTimeDesc(requestId).stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> listBidRequestIdsByArtisan(Long artisanId) {
        return bidMapper.findByArtisanIdOrderByCreateTimeDesc(artisanId).stream()
                .map(CustomRequestBid::getRequestId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public long countPendingBids(Long requestId) {
        return bidMapper.countByRequestIdAndStatus(requestId, "PENDING");
    }

    @Override
    @Transactional
    public SelectBidResult selectBid(Long requestId, Long buyerId, Long bidId) {
        CustomRequest request = requestMapper.findById(requestId)
                .orElseThrow(() -> new RuntimeException("需求不存在"));
        if (request.getBuyer() == null || !request.getBuyer().getId().equals(buyerId)) {
            throw new RuntimeException("仅需求发起人可选择合作对象");
        }
        if (!"OPEN".equals(request.getStatus())) {
            throw new RuntimeException("该需求已结束招募");
        }

        CustomRequestBid bid = bidMapper.findById(bidId)
                .orElseThrow(() -> new RuntimeException("揭榜记录不存在"));
        if (!requestId.equals(bid.getRequestId())) {
            throw new RuntimeException("揭榜记录与需求不匹配");
        }
        if (!"PENDING".equals(bid.getStatus())) {
            throw new RuntimeException("该揭榜已处理");
        }

        User artisan = userMapper.findById(bid.getArtisanId())
                .orElseThrow(() -> new RuntimeException("手作达人不存在"));

        double price = averageBudget(request.getBudgetMin(), request.getBudgetMax());

        CustomOrder order = new CustomOrder();
        order.setBuyerId(buyerId);
        order.setBuyerName(request.getBuyer().getUsername());
        order.setArtisanId(artisan.getId());
        order.setArtisanName(artisan.getUsername());
        order.setProductTitle(request.getTitle());
        order.setProductType("CUSTOM");
        order.setPrice(price);
        order.setRequirements(buildRequirements(request, bid.getMessage()));
        order.setCustomRequestId(requestId);
        order.setStatus("PENDING_CONFIRM");
        CustomOrder savedOrder = orderService.createOrder(order);

        bid.setStatus("SELECTED");
        bidMapper.save(bid);

        bidMapper.findByRequestIdOrderByCreateTimeDesc(requestId).forEach(other -> {
            if (!other.getId().equals(bidId) && "PENDING".equals(other.getStatus())) {
                other.setStatus("REJECTED");
                bidMapper.save(other);
                userMapper.findById(other.getArtisanId()).ifPresent(rejected ->
                        notificationService.notify(
                                rejected.getId(),
                                "REQUEST_BID",
                                "揭榜未选中",
                                "需求「" + request.getTitle() + "」已与其他手作达人合作",
                                "/account?menu=buyer-orders"));
            }
        });

        request.setStatus("MATCHED");
        CustomRequest savedRequest = requestMapper.save(request);

        notificationService.notify(
                artisan.getId(),
                "REQUEST_BID",
                "揭榜被选中",
                "买家选择了与你合作「" + request.getTitle() + "」，请尽快确认订单",
                "/account?menu=artisan-orders");

        SelectBidResult result = new SelectBidResult();
        result.setOrder(savedOrder);
        result.setRequest(savedRequest);
        return result;
    }

    private CustomRequestBidView toView(CustomRequestBid bid) {
        CustomRequestBidView view = new CustomRequestBidView();
        view.setId(bid.getId());
        view.setRequestId(bid.getRequestId());
        view.setArtisanId(bid.getArtisanId());
        view.setMessage(bid.getMessage());
        view.setStatus(bid.getStatus());
        view.setCreateTime(bid.getCreateTime());
        userMapper.findById(bid.getArtisanId()).ifPresent(user -> {
            view.setArtisanUsername(user.getUsername());
            view.setArtisanAvatar(user.getAvatar());
            view.setRating(user.getRating());
            view.setCreditScore(user.getCreditScore());
            view.setReviewCount(user.getReviewCount());
        });
        return view;
    }

    private double averageBudget(Double min, Double max) {
        double low = min != null ? min : 0;
        double high = max != null ? max : low;
        if (high < low) {
            double tmp = low;
            low = high;
            high = tmp;
        }
        if (high <= 0 && low <= 0) {
            return 100.0;
        }
        return Math.round((low + high) / 2.0 * 100.0) / 100.0;
    }

    private String buildRequirements(CustomRequest request, String bidMessage) {
        StringBuilder sb = new StringBuilder();
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            sb.append(request.getDescription().trim());
        }
        if (request.getDeadline() != null && !request.getDeadline().isBlank()) {
            sb.append("\n\n期望交付：").append(request.getDeadline());
        }
        if (bidMessage != null && !bidMessage.isBlank()) {
            sb.append("\n\n手作达人留言：").append(bidMessage.trim());
        }
        return sb.toString().trim();
    }
}
