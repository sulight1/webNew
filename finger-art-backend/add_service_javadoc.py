#!/usr/bin/env python3
"""为 service 包 Java 文件添加/修正中文 Javadoc。"""
import re
from pathlib import Path

SERVICE_DIR = Path(__file__).parent / "src/main/java/com/example/fingerartbackend/service"

IMPL_TO_IFACE = {
    "AdminExportServiceImpl": "AdminExportService",
    "AiChatServiceImpl": "AiChatService",
    "AnalyticsServiceImpl": "AnalyticsService",
    "CoinEconomyServiceImpl": "CoinEconomyService",
    "CraftTechniqueServiceImpl": "CraftTechniqueService",
    "CustomRequestBidServiceImpl": "CustomRequestBidService",
    "CustomRequestServiceImpl": "CustomRequestService",
    "DashScopeAiImageService": "AiImageService",
    "DemandMatchServiceImpl": "DemandMatchService",
    "ForumServiceImpl": "ForumService",
    "InspirationGachaServiceImpl": "InspirationGachaService",
    "LikeServiceImpl": "LikeService",
    "LogisticsServiceImpl": "LogisticsService",
    "NotificationServiceImpl": "NotificationService",
    "OrderServiceImpl": "OrderService",
    "ProductServiceImpl": "ProductService",
    "ReportServiceImpl": "ReportService",
    "ReviewServiceImpl": "ReviewService",
    "ScheduleSlotServiceImpl": "ScheduleSlotService",
    "SensitiveWordServiceImpl": "SensitiveWordService",
    "SkillExchangeServiceImpl": "SkillExchangeService",
    "SkillServiceImpl": "SkillService",
    "UserPunishmentServiceImpl": "UserPunishmentService",
    "UserServiceImpl": "UserService",
    "WalletServiceImpl": "WalletService",
}

CLASS_DOCS = {
    "AdminAuditService": "管理员操作审计服务，记录管理员关键操作并支持分页查询。",
    "AdminExportService": "管理端数据导出服务接口，提供用户与订单 Excel 导出。",
    "AdminTotpService": "管理员 TOTP 二次验证服务，负责绑定、启用、关闭及登录验证。",
    "AiChatService": "AI 对话服务接口，提供智能客服、作品推荐与分类推断。",
    "AiImageService": "AI 生图服务接口，根据提示词生成并保存图片。",
    "AnalyticsService": "数据分析服务接口，提供平台级与手作人维度的统计。",
    "CoinEconomyService": "造物币经济服务接口，管理签到、每日任务与作品推广。",
    "CraftTechniqueService": "工艺技法服务接口，维护手作分类下的技法字典。",
    "CustomRequestBidService": "定制需求揭榜服务接口，管理手作人对需求的投标与选中。",
    "CustomRequestService": "定制需求服务接口，负责发布、搜索与审核。",
    "DemandMatchService": "需求匹配服务接口，按技能与排期为需求推荐手作人。",
    "ForumService": "社区论坛服务接口，管理帖子、回复、点赞与管理员审核。",
    "InspirationGachaService": "灵感扭蛋服务接口，提供随机创作灵感与参考图生成。",
    "LikeService": "通用点赞服务接口，支持多类型目标的点赞状态管理。",
    "LogisticsService": "物流查询服务接口，查询订单快递轨迹。",
    "NotificationService": "平台通知服务接口，发送与管理用户站内通知。",
    "OrderService": "订单服务接口，覆盖定制/成品订单全生命周期与批量结算。",
    "PasswordService": "密码编解码服务，支持 BCrypt 加密与旧密码兼容校验。",
    "ProductService": "作品（商品）服务接口，管理上架、审核、互动与搜索。",
    "ReportService": "内容举报服务接口，处理用户举报与管理员处置。",
    "ReviewService": "评价服务接口，管理订单/交换评价与信用同步。",
    "ScheduleSlotService": "手作人排期服务接口，管理日历档期状态。",
    "SearchService": "全站搜索服务，聚合作品、定制需求与技能结果。",
    "SensitiveWordService": "敏感词服务接口，校验文本并维护词库。",
    "SkillExchangeService": "技能交换服务接口，管理交换请求与履约流程。",
    "SkillService": "技能发布服务接口，管理技能上架与审核。",
    "TotpService": "TOTP 工具服务，生成密钥、二维码并校验动态码。",
    "UserPunishmentService": "用户处罚服务接口，管理禁言、禁下单等限制。",
    "UserService": "用户服务接口，负责注册登录、达人申请与社交关系。",
    "WalletService": "钱包服务接口，管理充值、提现与交易流水。",
    "AdminExportServiceImpl": "管理端 Excel 导出实现，生成用户与订单报表。",
    "AiChatServiceImpl": "AI 对话实现，对接通义千问并提供智能兜底回复。",
    "AnalyticsServiceImpl": "数据分析实现，聚合平台趋势与手作人经营指标。",
    "CoinEconomyServiceImpl": "造物币经济实现，处理签到任务与曝光推广扣费。",
    "CraftTechniqueServiceImpl": "工艺技法实现，初始化默认技法并去重维护。",
    "CustomRequestBidServiceImpl": "定制揭榜实现，校验权限并创建匹配订单。",
    "CustomRequestServiceImpl": "定制需求实现，支持条件搜索、发布与审核通知。",
    "DashScopeAiImageService": "通义万相 AI 生图实现，异步生图并保存至本地。",
    "DemandMatchServiceImpl": "需求匹配实现，多维度评分推荐合适手作人。",
    "ForumServiceImpl": "论坛实现，处理发帖回帖、敏感词与点赞计数。",
    "InspirationGachaServiceImpl": "灵感扭蛋实现，随机组合风格/工艺/配色灵感。",
    "LikeServiceImpl": "点赞实现，持久化用户点赞关系。",
    "LogisticsServiceImpl": "物流查询实现，对接快递100并返回轨迹或兜底链接。",
    "NotificationServiceImpl": "通知实现，持久化通知并通过 WebSocket 推送。",
    "OrderServiceImpl": "订单实现，处理定金托管、发货、纠纷与批量下单。",
    "ProductServiceImpl": "作品实现，管理审核、库存、曝光排序与收藏。",
    "ReportServiceImpl": "举报实现，提交举报并执行下架/警告等处置。",
    "ReviewServiceImpl": "评价实现，提交评价并同步信用分与技能评分。",
    "ScheduleSlotServiceImpl": "排期实现，按月份查询并保存档期状态。",
    "SensitiveWordServiceImpl": "敏感词实现，启动初始化词库并校验文本。",
    "SkillExchangeServiceImpl": "技能交换实现，管理交换状态机与超时处理。",
    "SkillServiceImpl": "技能实现，管理发布、审核与用户技能列表。",
    "UserPunishmentServiceImpl": "用户处罚实现，施加与解除各类账号限制。",
    "UserServiceImpl": "用户实现，处理认证、达人流程与关注关系。",
    "WalletServiceImpl": "钱包实现，处理充值提现预下单与确认。",
}

METHOD_DOCS = {
    "AdminAuditService": {
        "log": ("记录当前管理员的操作日志。", {"action": "操作类型", "targetType": "目标实体类型", "targetId": "目标ID", "detail": "操作详情"}, None, "非管理员时静默跳过"),
        "listLogs": ("分页查询操作日志，按创建时间倒序。", {"page": "页码，从0开始", "size": "每页条数，1-100"}, "操作日志分页", None),
    },
    "AdminExportService": {
        "exportUsersExcel": ("导出全部用户为 Excel。", {}, "xlsx 字节数组", "IO 失败"),
        "exportOrdersExcel": ("导出全部订单为 Excel。", {}, "xlsx 字节数组", "IO 失败"),
    },
    "AdminTotpService": {
        "getStatus": ("查询当前管理员 TOTP 启用状态。", {}, "状态响应", "非管理员"),
        "setup": ("生成 TOTP 绑定密钥与二维码。", {}, "绑定信息", "已启用或非管理员"),
        "enable": ("验证动态码后启用 TOTP。", {"secret": "密钥", "code": "6位验证码"}, None, "验证码错误或已启用"),
        "disable": ("验证动态码后关闭 TOTP。", {"code": "6位验证码"}, None, "未启用或验证码错误"),
        "verifyLoginTotp": ("登录预认证阶段校验 TOTP。", {"preAuthToken": "预认证令牌", "code": "验证码"}, "管理员用户", "令牌失效或验证码错误"),
    },
    "AiChatService": {
        "chat": ("AI 对话，可结合订单与推荐上下文。", {"messages": "消息列表", "userId": "用户ID", "pageContext": "页面上下文"}, "回复、来源及推荐等", "消息为空"),
        "recommend": ("按关键词推荐作品。", {"query": "搜索词", "limit": "数量上限"}, "关键词与作品列表", None),
        "classifyProductCategory": ("推断作品分类。", {"keywords": "描述关键词", "imageUrl": "参考图URL"}, "category 与 source", None),
    },
    "AiImageService": {
        "generateAndSave": ("根据提示词生成图片并保存。", {"prompt": "生图提示词"}, "可访问的图片 URL", "API 未配置或生图失败"),
    },
    "AnalyticsService": {
        "getPlatformAnalytics": ("获取平台整体运营统计。", {}, "分类趋势、订单分布等", None),
        "getArtisanAnalytics": ("获取指定手作人经营数据。", {"userId": "用户ID"}, "作品、订单、收入等指标", "用户不存在"),
    },
    "CoinEconomyService": {
        "checkIn": ("每日签到领取造物币。", {"userId": "用户ID"}, "领取结果与余额", "今日已签到"),
        "getTaskStatus": ("查询每日任务领取状态。", {"userId": "用户ID"}, "任务列表", None),
        "claimDailyTask": ("领取指定每日任务奖励。", {"userId": "用户ID", "taskCode": "任务编码"}, "领取结果", "未知任务或已领取"),
        "grantEventReward": ("发放一次性事件奖励（幂等）。", {"userId": "用户ID", "taskCode": "任务编码", "referenceId": "关联ID", "coins": "币数", "title": "通知标题"}, None, None),
        "boostProductExposure": ("消耗造物币推广作品曝光。", {"userId": "用户ID", "productId": "作品ID"}, "更新后的作品", "非本人作品或余额不足"),
    },
    "CraftTechniqueService": {
        "getTechniquesByCategory": ("按分类查询技法。", {"category": "分类key"}, "技法列表", None),
        "getAllTechniques": ("查询全部技法。", {}, "技法列表", None),
        "initSampleData": ("初始化示例技法数据。", {}, "技法列表", None),
    },
    "CustomRequestBidService": {
        "submitBid": ("手作人提交揭榜。", {"requestId": "需求ID", "artisanId": "手作人ID", "message": "留言"}, "揭榜记录", "需求不可揭榜或重复揭榜"),
        "listBidsForRequest": ("查看需求的揭榜列表。", {"requestId": "需求ID", "viewerUserId": "查看者ID"}, "揭榜视图列表", "无权查看"),
        "listBidRequestIdsByArtisan": ("查询手作人揭榜过的需求ID。", {"artisanId": "手作人ID"}, "需求ID列表", None),
        "countPendingBids": ("统计待处理揭榜数。", {"requestId": "需求ID"}, "数量", None),
        "selectBid": ("买家选中揭榜并创建订单。", {"requestId": "需求ID", "buyerId": "买家ID", "bidId": "揭榜ID"}, "订单与需求", "无权或揭榜已处理"),
    },
    "CustomRequestService": {
        "search": ("分页搜索定制需求。", {"status": "状态", "category": "分类", "keyword": "关键词", "sort": "排序", "page": "页码", "size": "每页条数"}, "分页结果", None),
        "createRequest": ("买家发布定制需求。", {"payload": "需求字段"}, "待审核需求", "敏感词或用户不存在"),
        "auditRequest": ("管理员审核需求。", {"id": "需求ID", "status": "OPEN或REJECTED"}, "更新后的需求", "状态无效或不在待审核"),
    },
    "DemandMatchService": {
        "matchArtisansForRequest": ("为需求匹配手作人。", {"requestId": "需求ID", "limit": "返回数量"}, "匹配结果列表", "需求不存在"),
        "matchRequestsForArtisan": ("为手作人匹配可接需求。", {"artisanId": "手作人ID", "limit": "返回数量"}, "匹配结果列表", "用户不存在"),
        "notifyMatchedArtisans": ("向高分匹配手作人发送通知。", {"request": "已通过的需求"}, None, None),
    },
    "ForumService": {
        "listPosts": ("公开帖子列表。", {"sort": "排序hot/latest", "viewerId": "浏览者ID"}, "帖子列表", None),
        "listMyPosts": ("我的帖子列表。", {"authorId": "作者ID"}, "帖子列表", "未登录"),
        "getPost": ("获取帖子详情。", {"id": "帖子ID", "incrementView": "是否增加浏览量"}, "帖子", "不存在或已删除"),
        "createPost": ("发布帖子。", {"authorId": "作者ID", "title": "标题", "content": "内容", "imageUrl": "图片URL"}, "帖子", "禁言或敏感词"),
        "deletePost": ("删除帖子（软删）。", {"id": "帖子ID", "operatorId": "操作者ID"}, None, "无权删除"),
        "listReplies": ("帖子回复列表。", {"postId": "帖子ID"}, "回复列表", None),
        "createReply": ("发表回复。", {"postId": "帖子ID", "authorId": "作者ID", "content": "内容"}, "回复", "禁言或内容为空"),
        "toggleLikePost": ("切换帖子点赞。", {"postId": "帖子ID", "userId": "用户ID"}, "点赞结果", None),
        "getPostDetail": ("帖子详情含回复。", {"id": "帖子ID", "viewerId": "浏览者ID"}, "详情Map", None),
        "listPostsForAdmin": ("管理员帖子列表。", {"status": "状态", "keyword": "关键词"}, "帖子列表", None),
        "getPostDetailForAdmin": ("管理员查看帖子详情。", {"id": "帖子ID"}, "详情Map", "帖子不存在"),
        "deleteReply": ("删除回复。", {"id": "回复ID", "operatorId": "操作者ID"}, None, "无权删除"),
        "restorePost": ("管理员恢复已删帖子。", {"id": "帖子ID"}, None, "非管理员"),
    },
    "InspirationGachaService": {
        "getStatus": ("查询扭蛋免费次数与余额。", {"userId": "用户ID"}, "状态Map", "用户不存在"),
        "draw": ("抽取灵感。", {"userId": "用户ID", "useFree": "是否使用免费次数"}, "灵感结果", "免费已用完或余额不足"),
        "generateImage": ("根据提示词生成参考图。", {"userId": "用户ID", "imagePrompt": "生图描述"}, "图片URL与余额", "余额不足或生图失败"),
    },
    "LikeService": {
        "toggle": ("切换点赞状态。", {"userId": "用户ID", "targetType": "目标类型", "targetId": "目标ID"}, "操作后是否已点赞", "未登录"),
        "isLiked": ("是否已点赞。", {"userId": "用户ID", "targetType": "目标类型", "targetId": "目标ID"}, "是否点赞", None),
        "getLikedTargetIds": ("批量查询已点赞目标ID。", {"userId": "用户ID", "targetType": "目标类型", "targetIds": "目标ID集合"}, "已点赞ID集合", None),
    },
    "LogisticsService": {
        "queryOrderLogistics": ("查询订单物流轨迹。", {"orderId": "订单ID", "userId": "用户ID"}, "物流结果", "无权或无单号"),
    },
    "NotificationService": {
        "notify": ("发送站内通知。", {"userId": "用户ID", "type": "类型", "title": "标题", "content": "内容", "linkUrl": "跳转链接"}, "通知实体", None),
        "getUserNotifications": ("用户通知列表。", {"userId": "用户ID"}, "通知列表", None),
        "getUnreadCount": ("未读通知数量。", {"userId": "用户ID"}, "未读数", None),
        "markRead": ("标记单条已读。", {"notificationId": "通知ID", "userId": "用户ID"}, None, "通知不存在或无权"),
        "markAllRead": ("全部标记已读。", {"userId": "用户ID"}, None, None),
        "deleteAllRead": ("删除全部已读通知。", {"userId": "用户ID"}, "删除数量", None),
    },
    "OrderService": {
        "createOrder": ("创建订单。", {"order": "订单实体"}, "已保存订单", "禁止下单或库存不足"),
        "getArtisanOrders": ("手作人订单列表。", {"artisanId": "手作人ID"}, "订单列表", None),
        "getBuyerOrders": ("买家订单列表。", {"buyerId": "买家ID"}, "订单列表", None),
        "getAllOrders": ("全部订单（可按状态筛选）。", {"status": "状态，可空"}, "订单列表", None),
        "getOrder": ("订单详情。", {"id": "订单ID"}, "订单", "不存在"),
        "confirmOrder": ("手作人确认订单。", {"orderId": "订单ID", "artisanId": "手作人ID"}, "订单", "无权或状态不允许"),
        "payDeposit": ("支付定金或成品全款。", {"orderId": "订单ID", "buyerId": "买家ID", "paymentChannel": "支付渠道"}, "订单", "无权或状态不允许"),
        "payBalance": ("支付尾款。", {"orderId": "订单ID", "buyerId": "买家ID"}, "订单", "无权或状态不允许"),
        "confirmReceipt": ("买家确认收货。", {"orderId": "订单ID", "buyerId": "买家ID"}, "订单", "无权或状态不允许"),
        "updateStatus": ("更新订单状态。", {"orderId": "订单ID", "status": "新状态", "operatorId": "操作者ID", "operatorName": "操作者名"}, "订单", "状态不允许"),
        "shipOrder": ("发货并填写物流。", {"orderId": "订单ID", "artisanId": "手作人ID", "shippingCompany": "快递公司", "trackingNumber": "单号", "operatorName": "操作者名"}, "订单", "无权或状态不允许"),
        "addMilestone": ("添加订单里程碑。", {"orderId": "订单ID", "payload": "里程碑字段", "operatorId": "操作者ID", "operatorName": "操作者名"}, "订单", None),
        "getMilestones": ("订单里程碑列表。", {"orderId": "订单ID"}, "里程碑列表", None),
        "getEscrowTransactions": ("托管交易流水。", {"orderId": "订单ID"}, "流水列表", None),
        "openDispute": ("发起纠纷。", {"orderId": "订单ID", "userId": "用户ID", "reason": "原因"}, "订单", "无权"),
        "resolveDispute": ("管理员解决纠纷。", {"orderId": "订单ID", "releaseToArtisan": "是否释放给手作人"}, "订单", None),
        "requestCancel": ("买家申请取消。", {"orderId": "订单ID", "buyerId": "买家ID", "reason": "原因"}, "订单", "无权"),
        "approveCancel": ("手作人同意取消。", {"orderId": "订单ID", "artisanId": "手作人ID"}, "订单", "无权"),
        "rejectCancel": ("手作人拒绝取消。", {"orderId": "订单ID", "artisanId": "手作人ID", "reason": "原因"}, "订单", "无权"),
        "deleteOrder": ("删除订单。", {"id": "订单ID"}, None, None),
        "batchCheckoutReadyMade": ("批量结算成品购物车。", {"request": "批量下单请求"}, "批量结果", "库存或地址校验失败"),
    },
    "PasswordService": {
        "encode": ("BCrypt 加密明文密码。", {"rawPassword": "明文密码"}, "密文", None),
        "matches": ("校验密码是否匹配。", {"rawPassword": "明文", "storedPassword": "存储密码"}, "是否匹配", None),
        "needsUpgrade": ("判断是否需升级为 BCrypt。", {"storedPassword": "存储密码"}, "是否需要升级", None),
    },
    "ProductService": {
        "getAllProducts": ("全部作品（含未审核）。", {"viewerId": "浏览者ID"}, "作品列表", None),
        "getProductById": ("作品详情。", {"id": "作品ID", "viewerId": "浏览者ID"}, "作品", "不存在"),
        "getApprovedProducts": ("已审核且有库存作品。", {"viewerId": "浏览者ID"}, "作品列表", None),
        "getApprovedProductsByType": ("按类型筛选已审核作品。", {"type": "类型", "viewerId": "浏览者ID"}, "作品列表", None),
        "getApprovedProductsByCategory": ("按分类筛选。", {"category": "分类", "viewerId": "浏览者ID"}, "作品列表", None),
        "getApprovedProductsByCraftTechnique": ("按工艺筛选。", {"craftTechnique": "工艺", "viewerId": "浏览者ID"}, "作品列表", None),
        "getApprovedProductsByCreatorId": ("创作者作品列表。", {"creatorId": "创作者ID", "viewerId": "浏览者ID"}, "作品列表", None),
        "createInitialProduct": ("创建草稿作品。", {}, "草稿作品", None),
        "saveProduct": ("保存作品。", {"product": "作品实体"}, "作品", "敏感词"),
        "toggleLikeProduct": ("切换作品点赞。", {"id": "作品ID", "userId": "用户ID"}, "点赞结果", None),
        "toggleFavoriteProduct": ("切换作品收藏。", {"id": "作品ID", "userId": "用户ID"}, "收藏结果", None),
        "deleteProduct": ("删除作品。", {"id": "作品ID"}, None, None),
        "auditProduct": ("审核作品。", {"id": "作品ID", "status": "审核状态"}, "作品", None),
        "batchAuditProducts": ("批量审核作品。", {"ids": "ID列表", "status": "状态"}, "成功数量", None),
        "updateProduct": ("更新作品信息。", {"id": "作品ID", "product": "更新内容", "operatorUserId": "操作者ID"}, "作品", "无权"),
        "updateStock": ("更新库存。", {"id": "作品ID", "stock": "库存"}, "作品", None),
        "searchApprovedProducts": ("搜索已审核作品。", {"q": "关键词", "limit": "数量", "viewerId": "浏览者ID"}, "作品列表", None),
        "getHotProducts": ("热门作品。", {"limit": "数量", "viewerId": "浏览者ID"}, "作品列表", None),
        "getSimilarProducts": ("相似作品推荐。", {"id": "作品ID", "limit": "数量", "viewerId": "浏览者ID"}, "作品列表", None),
        "getFavoriteProducts": ("用户收藏的作品。", {"userId": "用户ID"}, "作品列表", None),
    },
    "ReportService": {
        "submitReport": ("提交内容举报。", {"report": "举报实体"}, "举报记录", "未登录或信息不完整"),
        "listReports": ("举报列表。", {"status": "状态筛选"}, "举报列表", None),
        "handleReport": ("处理举报。", {"id": "举报ID", "handlerId": "处理人ID", "action": "处置动作", "handleNote": "备注"}, "举报记录", "已处理"),
        "countPending": ("待处理举报数量。", {}, "数量", None),
    },
    "ReviewService": {
        "submitReview": ("提交评价。", {"payload": "评价字段"}, "评价", "重复评价或无权"),
        "getReviewsForUser": ("用户收到的评价。", {"userId": "用户ID"}, "评价视图列表", None),
        "getProductReviews": ("作品评价列表。", {"productId": "作品ID"}, "评价列表", None),
        "getProductReviewEligibility": ("是否可评价作品。", {"productId": "作品ID", "userId": "用户ID"}, "资格信息", None),
        "hasReviewedOrder": ("是否已评价订单。", {"orderId": "订单ID", "fromUserId": "评价人ID"}, "是否已评", None),
        "hasReviewedExchange": ("是否已评价交换。", {"exchangeId": "交换ID", "fromUserId": "评价人ID"}, "是否已评", None),
        "completeExchangeWithReview": ("双方评价完成后结束交换。", {"exchangeId": "交换ID"}, "交换实体", None),
        "getOrderReviewDetail": ("订单评价详情。", {"orderId": "订单ID", "userId": "用户ID"}, "详情Map", None),
        "deleteReview": ("删除评价。", {"reviewId": "评价ID", "userId": "用户ID"}, None, "无权"),
        "deleteReviewReply": ("删除评价回复。", {"replyId": "回复ID", "userId": "用户ID"}, None, "无权"),
        "appendReviewReply": ("追加评价回复。", {"reviewId": "评价ID", "userId": "用户ID", "content": "内容", "imageUrls": "图片"}, "回复", "无权"),
    },
    "ScheduleSlotService": {
        "getSlotsByUserIdAndMonth": ("查询用户某月排期。", {"userId": "用户ID", "year": "年", "month": "月"}, "档期列表", None),
        "saveOrUpdateSlot": ("保存或更新单日排期。", {"userId": "用户ID", "date": "日期", "status": "状态", "remark": "备注"}, "档期", None),
    },
    "SearchService": {
        "search": ("全站关键词搜索。", {"q": "关键词", "limit": "结果上限"}, "搜索结果项列表", None),
    },
    "SensitiveWordService": {
        "validateText": ("校验文本是否含敏感词。", {"text": "文本", "fieldLabel": "字段名"}, None, "含敏感词"),
        "listWords": ("敏感词列表。", {}, "词列表", None),
        "listWordDetails": ("敏感词详情列表。", {}, "详情列表", None),
        "addWord": ("添加敏感词。", {"word": "词语"}, None, None),
        "removeWord": ("删除敏感词。", {"id": "词ID"}, None, None),
        "initDefaults": ("初始化默认敏感词。", {}, None, None),
    },
    "SkillExchangeService": {
        "requestExchange": ("发起技能交换请求。", {"userAId": "发起人ID", "userBId": "对方ID", "description": "描述", "cost": "造物币", "scheduleDate": "约定日期"}, "交换记录", "禁止交换"),
        "acceptExchange": ("接受交换。", {"exchangeId": "交换ID", "userId": "用户ID"}, "交换", "无权或状态不对"),
        "confirmExchange": ("确认交换。", {"exchangeId": "交换ID", "userId": "用户ID"}, "交换", "无权"),
        "completeExchange": ("完成交换。", {"exchangeId": "交换ID", "userId": "用户ID"}, "交换", "无权"),
        "reportNoShow": ("举报对方爽约。", {"exchangeId": "交换ID", "reporterId": "举报人ID"}, "交换", None),
        "getMyExchanges": ("我的交换列表。", {"userId": "用户ID"}, "交换列表", None),
        "processOverdueExchanges": ("处理超时未完成的交换。", {}, None, None),
    },
    "SkillService": {
        "getAllSkills": ("全部技能。", {}, "技能列表", None),
        "getApprovedSkills": ("已审核技能。", {}, "技能列表", None),
        "getApprovedSkillsByCategory": ("按分类查已审核技能。", {"category": "分类"}, "技能列表", None),
        "getSkillsByCategory": ("按分类查技能。", {"category": "分类"}, "技能列表", None),
        "saveSkill": ("保存技能。", {"skill": "技能实体"}, "技能", "敏感词"),
        "deleteSkill": ("删除技能。", {"id": "技能ID"}, None, None),
        "auditSkill": ("审核技能。", {"id": "技能ID", "status": "状态"}, "技能", None),
        "getApprovedSkillsByUserId": ("用户已审核技能。", {"userId": "用户ID"}, "技能列表", None),
        "getMySkills": ("我的技能列表。", {"userId": "用户ID"}, "技能列表", None),
        "updateSkill": ("用户更新自己的技能。", {"id": "技能ID", "userId": "用户ID", "patch": "更新内容"}, "技能", "无权"),
        "deleteSkillByUser": ("用户删除自己的技能。", {"id": "技能ID", "userId": "用户ID"}, None, "无权"),
    },
    "TotpService": {
        "generateSecret": ("生成 TOTP 密钥。", {}, "密钥字符串", None),
        "buildOtpAuthUrl": ("构建 otpauth 绑定 URL。", {"account": "账号", "secret": "密钥"}, "URL", None),
        "generateQrDataUri": ("生成二维码 Data URI。", {"account": "账号", "secret": "密钥"}, "Data URI", "二维码生成失败"),
        "verifyCode": ("校验 TOTP 动态码。", {"secret": "密钥", "code": "验证码"}, "是否有效", None),
    },
    "UserPunishmentService": {
        "getActiveViews": ("用户当前生效的处罚视图。", {"userId": "用户ID"}, "处罚视图列表", None),
        "applyPunishments": ("施加处罚。", {"userId": "用户ID", "adminId": "管理员ID", "request": "处罚请求"}, "处罚记录列表", None),
        "liftPunishment": ("解除单项处罚。", {"userId": "用户ID", "type": "处罚类型", "adminId": "管理员ID"}, None, None),
        "liftPunishments": ("批量解除处罚。", {"userId": "用户ID", "types": "类型列表", "adminId": "管理员ID"}, None, None),
        "liftAllPunishments": ("解除全部处罚。", {"userId": "用户ID", "adminId": "管理员ID"}, None, None),
        "isPunished": ("是否处于某类处罚中。", {"userId": "用户ID", "type": "类型"}, "是否处罚中", None),
        "isAccountBanned": ("账号是否被封禁。", {"userId": "用户ID"}, "是否封禁", None),
        "assertNotPunished": ("断言未受处罚，否则抛异常。", {"userId": "用户ID", "type": "类型", "message": "提示信息"}, None, "受处罚时"),
    },
    "UserService": {
        "register": ("用户注册。", {"request": "注册请求"}, "用户", "账号已存在"),
        "applyArtisan": ("申请成为手作达人。", {"userId": "用户ID"}, "用户", None),
        "approveArtisan": ("通过达人申请。", {"userId": "用户ID"}, "用户", None),
        "rejectArtisan": ("拒绝达人申请。", {"userId": "用户ID"}, "用户", None),
        "listPendingArtisanApplications": ("待审核达人列表。", {}, "用户列表", None),
        "login": ("账号密码登录。", {"account": "账号", "password": "密码"}, "用户", "账号或密码错误"),
        "getAllUsers": ("全部用户。", {}, "用户列表", None),
        "deleteUser": ("删除用户。", {"id": "用户ID"}, None, None),
        "updateUser": ("更新用户信息。", {"id": "用户ID", "user": "更新内容"}, "用户", None),
        "resetPassword": ("重置密码。", {"id": "用户ID", "newPassword": "新密码"}, "用户", None),
        "requestPasswordReset": ("申请密码重置。", {"account": "账号"}, None, None),
        "addZaoWuBi": ("增减造物币余额。", {"userId": "用户ID", "amount": "变动金额"}, "用户", "用户不存在"),
        "getUserById": ("按ID查用户。", {"id": "用户ID"}, "用户", "不存在"),
        "follow": ("关注用户。", {"followerId": "关注者ID", "followingId": "被关注者ID"}, None, None),
        "unfollow": ("取消关注。", {"followerId": "关注者ID", "followingId": "被关注者ID"}, None, None),
        "isFollowing": ("是否已关注。", {"followerId": "关注者ID", "followingId": "被关注者ID"}, "是否关注", None),
        "getFollowers": ("粉丝列表。", {"userId": "用户ID"}, "用户列表", None),
        "getFollowings": ("关注列表。", {"userId": "用户ID"}, "用户列表", None),
        "getPublicProfile": ("公开资料页。", {"id": "用户ID"}, "公开资料", None),
        "listTopArtisans": ("推荐手作人列表。", {"limit": "数量", "excludeUserId": "排除用户ID"}, "用户列表", None),
    },
    "WalletService": {
        "recharge": ("直接充值（同步到账）。", {"userId": "用户ID", "amount": "金额", "channel": "渠道"}, "操作结果", "金额无效"),
        "withdraw": ("直接提现。", {"userId": "用户ID", "amount": "金额", "channel": "渠道"}, "操作结果", "余额不足"),
        "rechargePrepay": ("充值预下单。", {"userId": "用户ID", "amount": "金额", "channel": "渠道"}, "交易记录", None),
        "rechargeConfirm": ("确认充值到账。", {"userId": "用户ID", "outTradeNo": "外部单号"}, "操作结果", "单号无效"),
        "withdrawPrepay": ("提现预下单。", {"userId": "用户ID", "amount": "金额", "channel": "渠道"}, "交易记录", "余额不足"),
        "withdrawConfirm": ("确认提现完成。", {"userId": "用户ID", "outTradeNo": "外部单号"}, "操作结果", None),
        "getTransactions": ("分页查询钱包流水。", {"userId": "用户ID", "page": "页码", "size": "每页条数"}, "流水分页", None),
    },
}

PRIVATE_METHOD_DOCS = {
    "AdminExportServiceImpl": {
        "createHeaderStyle": "创建 Excel 表头样式。",
        "writeHeader": "写入表头行。",
        "setCell": "设置单元格值。",
        "autoSizeColumns": "自动调整列宽。",
        "labelRole": "角色转中文标签。",
        "labelArtisanApply": "达人申请状态转中文。",
        "labelPasswordReset": "密码重置状态转中文。",
        "labelProductType": "作品类型转中文。",
        "labelOrderStatus": "订单状态转中文。",
        "labelEscrowStatus": "托管状态转中文。",
        "formatDateTime": "格式化日期时间。",
    },
    "AdminAuditService": {"resolveClientIp": "解析客户端 IP。"},
    "AdminTotpService": {"requireAdmin": "校验当前用户为有效管理员。"},
    "PasswordService": {"isBcryptHash": "判断是否为 BCrypt 哈希。"},
    "SearchService": {"textMatch": "多字段关键词匹配。", "joinSubtitle": "拼接搜索结果副标题。"},
    "AiChatServiceImpl": {
        "categoryResult": "组装分类推断结果。", "callQwenCategory": "调用通义千问推断分类。",
        "hasApiKey": "是否已配置 API Key。", "resolveApiKey": "解析 API Key。",
        "callQwen": "调用通义千问对话。", "buildSystemPrompt": "构建系统提示词。",
        "buildOrderContext": "汇总买家订单上下文。", "formatOrderStatus": "订单状态转中文。",
        "searchProductsForMessage": "按消息搜索推荐作品。", "extractSearchKeywords": "提取搜索关键词。",
        "addSynonymKeywords": "扩展同义词。", "isStopWord": "判断停用词。",
        "toRecommendationList": "转推荐列表。", "resolveImage": "解析展示图 URL。",
        "buildRecommendReason": "生成推荐理由。", "buildActions": "生成快捷操作。",
        "action": "构建操作项。", "buildFallbackReply": "规则兜底回复。",
        "isOrderQuery": "是否订单相关提问。", "isProductQuery": "是否作品推荐提问。",
        "containsAny": "是否包含任一关键词。",
    },
    "AnalyticsServiceImpl": {"countByField": "按字段分组统计。"},
    "CoinEconomyServiceImpl": {"claimDailyInternal": "领取每日任务内部逻辑。", "taskRow": "构建任务状态行。"},
    "CraftTechniqueServiceImpl": {
        "ensureDefaultTechniquesOnce": "懒加载初始化默认技法。", "removeDuplicateRecords": "删除重复技法。",
        "seedMissingDefaults": "补全默认技法。", "deduplicate": "列表去重。", "techniqueKey": "技法唯一键。",
        "buildDefaultTechniques": "内置默认技法。", "create": "创建技法实体。",
    },
    "CustomRequestBidServiceImpl": {
        "toView": "揭榜转视图。", "averageBudget": "计算参考价。", "assertRecruiting": "校验可揭榜状态。", "buildRequirements": "合并订单要求。",
    },
    "CustomRequestServiceImpl": {"buildSpec": "构建查询条件。", "resolveSort": "解析排序。", "notifyBuyerAuditResult": "通知审核结果。"},
    "DashScopeAiImageService": {
        "createTask": "提交异步生图任务。", "pollTaskResult": "轮询生图结果。", "authHeaders": "构建认证头。",
        "saveRemoteImage": "保存远程图片到本地。", "downloadSignedUrl": "下载 OSS 预签名 URL。",
    },
    "DemandMatchServiceImpl": {
        "scoreArtisans": "为需求评分排序手作人。", "scoreArtisanForRequest": "单对手作人评分。", "hasFreeSlotBeforeDeadline": "截止前是否有空闲排期。",
    },
    "ForumServiceImpl": {"populateLikedStatus": "填充帖子点赞状态。", "containsIgnoreCase": "忽略大小写包含判断。"},
    "InspirationGachaServiceImpl": {
        "markFreeUsed": "记录免费扭蛋已用。", "buildRandomInspiration": "随机生成灵感包。", "buildCopy": "生成文案。",
        "buildTitle": "生成标题。", "buildDescription": "生成描述。", "buildImagePrompt": "生成生图提示词。",
        "suggestPrice": "建议售价。", "buildTags": "生成标签。",
    },
    "LogisticsServiceImpl": {
        "assertCanView": "校验物流查看权限。", "fillFromKuaidi100": "调用快递100 API。", "resolveCompanyCode": "快递公司编码。",
        "buildFallbackUrl": "构建兜底查询链接。", "md5Upper": "MD5 大写签名。",
    },
    "ProductServiceImpl": {
        "populateCreatorAvatar": "填充创作者头像。", "hasAvailableStock": "是否有可用库存。", "ensureDefaultStock": "设置默认库存。",
        "refreshBoost": "清除过期曝光加权。", "compareExposure": "曝光排序比较。", "effectiveBoost": "有效曝光加权。",
        "resolveCreatorId": "解析创作者 ID。", "backfillCreatorId": "补全 creatorId。", "ensureArtisanCreator": "校验手作达人身份。",
        "assertProductOwner": "校验作品归属。", "hasContentChanges": "判断内容是否变更。", "matchesKeyword": "关键词匹配。",
        "contains": "文本包含判断。", "similarTo": "是否相似作品。", "similarityScore": "相似度评分。",
        "notifyAuditResult": "通知审核结果。", "assertCanPublishProduct": "校验发布权限。",
    },
    "ReportServiceImpl": {"applyModerationAction": "执行举报处置。", "resolveTargetTitle": "解析举报目标标题。"},
    "ReviewServiceImpl": {
        "toUserReviewView": "评价转展示视图。", "enrichOrderReviewContext": "补充订单评价上下文。",
        "fallbackOrderTitle": "订单标题兜底。", "resolveExchangeContextTitle": "交换标题兜底。",
        "backfillFromUserProfile": "回填评价人最新资料。", "applyCreditAndRating": "更新信用与均分。",
        "syncSkillCredit": "同步技能信用。", "serializeImageUrls": "序列化评价图片 URL。",
    },
    "OrderServiceImpl": {
        "finishCancelWithRefund": "取消订单并退款。", "isCancellableStatus": "是否可取消状态。", "normalizeCancelStatus": "规范化取消状态。",
        "refundEscrowToBuyer": "托管款退还给买家。", "holdEscrow": "造物币托管扣款。", "releaseEscrowToArtisan": "托管款释放给手作人。",
        "recordEscrow": "记录托管流水。", "addMilestoneRecord": "写入里程碑。", "incrementCompletedOrders": "增加完成订单数。",
        "validateTransition": "校验状态流转。", "mapStatusToStage": "状态映射里程碑阶段。", "statusLabel": "状态中文标签。",
        "round": "金额四舍五入。", "normalizePaymentChannel": "规范化支付渠道。", "paymentChannelLabel": "支付渠道中文。",
        "orderQuantity": "订单数量。", "validateProductStock": "校验库存。", "deductProductStock": "扣减库存。",
        "restoreProductStock": "恢复库存。", "notifyArtisan": "通知手作人。", "notifyBuyer": "通知买家。",
        "notifyOrderStatusChange": "通知订单状态变更。", "safeTitle": "安全订单标题。", "safeName": "安全用户名。",
        "buildCustomRequirementsMilestoneNote": "定制需求里程碑说明。", "buildCustomOrderNotifyContent": "定制订单通知内容。",
        "isReadyMadeOrder": "是否成品订单。", "normalizeReadyMadePayment": "修正成品订单支付比例。", "normalizeReadyMadeStatus": "修正成品订单状态。",
        "hasPaidEscrow": "是否已支付托管。", "normalizeOrderForRead": "读取前规范化订单。", "applyShippingFromBuyer": "从买家资料填充收货地址。",
        "requireOrderShippingAddress": "校验收货地址。", "hasOrderShippingAddress": "是否已有收货地址。", "safeProductTitle": "安全作品标题。",
    },
    "UserServiceImpl": {"normalizeRole": "规范化用户角色字段。"},
    "SkillExchangeServiceImpl": {
        "getExchange": "获取交换记录。", "markProviderSlot": "标记排期状态。", "bumpCredit": "增加信用分。", "penalizeCredit": "扣减信用分。",
    },
    "UserPunishmentServiceImpl": {
        "toView": "处罚转视图。", "notifyLift": "通知处罚解除。", "notifyUser": "通知处罚生效。",
    },
    "WalletServiceImpl": {
        "getPendingTransaction": "获取待确认交易。", "savePendingTransaction": "保存预下单交易。",
        "validateAmount": "校验金额。", "generateOutTradeNo": "生成外部单号。", "round": "金额四舍五入。",
        "formatAmount": "格式化金额。", "rechargeChannelLabel": "充值渠道中文。",
    },
}


def format_method_doc(desc, params, ret, throws):
    lines = [f" * {desc}"]
    for p, pd in params.items():
        lines.append(f" * @param {p} {pd}")
    if ret:
        lines.append(f" * @return {ret}")
    if throws:
        lines.append(f" * @throws RuntimeException {throws}")
    return "\n".join(lines)


def make_javadoc(body_lines):
    return "/**\n" + body_lines + "\n */"


def get_method_doc_key(class_name):
    if class_name in METHOD_DOCS:
        return class_name
    if class_name.endswith("Impl") and class_name[:-4] in METHOD_DOCS:
        return class_name[:-4]
    if class_name in IMPL_TO_IFACE:
        return IMPL_TO_IFACE[class_name]
    if class_name == "DashScopeAiImageService":
        return "AiImageService"
    return class_name


def replace_class_javadoc(content, class_name):
    doc = CLASS_DOCS.get(class_name)
    if not doc:
        return content
    new_block = make_javadoc(f" * {doc}")
    # 替换已有类 Javadoc
    pat = rf"/\*\*[\s\S]*?\*/\s*\n((?:@\w+(?:\([^)]*\))?\s*\n)*)public (?:class|interface) {re.escape(class_name)}"
    if re.search(pat, content):
        return re.sub(rf"/\*\*[\s\S]*?\*/\s*\n(?=(?:@\w+(?:\([^)]*\))?\s*\n)*public (?:class|interface) {re.escape(class_name)})", new_block + "\n", content, count=1)
    pat2 = rf"((?:@\w+(?:\([^)]*\))?\s*\n)*)public (?:class|interface) {re.escape(class_name)}"
    return re.sub(pat2, new_block + "\n\\1public " + ("class" if "class " + class_name in content else "interface") + f" {class_name}", content, count=1)


def find_method_signature(content, method_name):
    """找到方法声明起始位置（跳过已有 javadoc）。"""
    patterns = [
        rf"(?:@\w+\s*\n\s*)*@Override\s*\n\s*(?:@\w+\s*\n\s*)*public [\w<>,\[\]\s.?]+\s+{re.escape(method_name)}\s*\(",
        rf"(?:@\w+\s*\n\s*)*@PostConstruct\s*\n\s*(?:@Override\s*\n\s*)?(?:@\w+\s*\n\s*)*public [\w<>,\[\]\s.?]+\s+{re.escape(method_name)}\s*\(",
        rf"(?:@\w+\s*\n\s*)*public [\w<>,\[\]\s.?]+\s+{re.escape(method_name)}\s*\(",
        rf"(?:@\w+\s*\n\s*)*private [\w<>,\[\]\s.?]+\s+{re.escape(method_name)}\s*\(",
    ]
    for pat in patterns:
        m = re.search(pat, content)
        if m:
            return m.start()
    return None


def strip_javadoc_before(content, pos):
    """移除 pos 前紧邻的 javadoc 块。"""
    before = content[:pos].rstrip()
    m = re.search(r'/\*\*[\s\S]*?\*/\s*$', before)
    if m:
        return before[:m.start()].rstrip() + "\n" + content[pos:]
    return content


def insert_method_javadoc(content, pos, javadoc):
    return content[:pos] + javadoc + "\n" + content[pos:]


def process_methods(content, doc_key, method_names):
    for method_name in method_names:
        docs = METHOD_DOCS.get(doc_key, {})
        if method_name not in docs:
            continue
        desc, params, ret, throws = docs[method_name]
        javadoc = make_javadoc(format_method_doc(desc, params, ret, throws))
        while True:
            pos = find_method_signature(content, method_name)
            if pos is None:
                break
            content = strip_javadoc_before(content, pos)
            pos = find_method_signature(content, method_name)
            if pos is None:
                break
            content = insert_method_javadoc(content, pos, javadoc)
    return content


def process_private_methods(content, class_name):
    privates = PRIVATE_METHOD_DOCS.get(class_name, {})
    for method_name, desc in privates.items():
        javadoc = make_javadoc(f" * {desc}")
        while True:
            pos = find_method_signature(content, method_name)
            if pos is None:
                break
            content = strip_javadoc_before(content, pos)
            pos = find_method_signature(content, method_name)
            if pos is None:
                break
            content = insert_method_javadoc(content, pos, javadoc)
    return content


def process_interface_methods(content, doc_key):
    """为接口中无注解的方法添加 javadoc。"""
    docs = METHOD_DOCS.get(doc_key, {})
    for method_name, (desc, params, ret, throws) in docs.items():
        # 接口方法：可能在已有 javadoc 后或直接 public
        pat = rf"(/\*\*[\s\S]*?\*/\s*\n\s*)?([\w<>,\[\]\s.?]+\s+{re.escape(method_name)}\s*\([^;]*\);)"
        def repl(m):
            if m.group(1):
                javadoc = make_javadoc(format_method_doc(desc, params, ret, throws))
                return javadoc + "\n    " + m.group(2).strip()
            javadoc = make_javadoc(format_method_doc(desc, params, ret, throws))
            return javadoc + "\n    " + m.group(2).strip()
        content = re.sub(pat, repl, content)
    return content


def process_file(path: Path):
    class_name = path.stem
    if class_name not in CLASS_DOCS:
        return False, class_name

    content = path.read_text(encoding="utf-8")
    original = content

    content = replace_class_javadoc(content, class_name)
    doc_key = get_method_doc_key(class_name)

    if path.parent.name != "impl" and f"interface {class_name}" in content:
        content = process_interface_methods(content, doc_key)
    else:
        method_names = list(METHOD_DOCS.get(doc_key, {}).keys())
        content = process_methods(content, doc_key, method_names)
        content = process_private_methods(content, class_name)

    if content != original:
        path.write_text(content, encoding="utf-8")
        return True, class_name
    return False, class_name


def main():
    files = sorted(SERVICE_DIR.rglob("*.java"))
    processed = []
    for f in files:
        changed, name = process_file(f)
        if changed:
            processed.append(str(f.relative_to(SERVICE_DIR.parent.parent.parent.parent)))
    print(f"TOTAL={len(processed)}")
    for p in processed:
        print(p)


if __name__ == "__main__":
    main()
