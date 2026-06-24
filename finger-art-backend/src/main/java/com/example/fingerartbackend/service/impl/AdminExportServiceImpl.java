package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.entity.CustomOrder;
import com.example.fingerartbackend.entity.User;
import com.example.fingerartbackend.mapper.UserMapper;
import com.example.fingerartbackend.service.AdminExportService;
import com.example.fingerartbackend.service.OrderService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 管理端服务实现类。
 */
@Service
public class AdminExportServiceImpl implements AdminExportService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Map<String, String> ROLE_LABELS = Map.of(
            "ADMIN", "管理员",
            "ARTISAN", "手作达人",
            "BUYER", "买家",
            "USER", "普通用户"
    );

    private static final Map<String, String> ORDER_STATUS_LABELS = Map.ofEntries(
            Map.entry("PENDING_CONFIRM", "待确认"),
            Map.entry("PENDING_PAY", "待支付"),
            Map.entry("PRODUCING", "制作中"),
            Map.entry("PENDING_SHIP", "待发货"),
            Map.entry("PENDING_ACCEPT", "待收货"),
            Map.entry("PENDING_BALANCE", "待付尾款"),
            Map.entry("COMPLETED", "已完成"),
            Map.entry("CANCELLED", "已取消"),
            Map.entry("DISPUTED", "纠纷中")
    );

    private static final Map<String, String> ESCROW_STATUS_LABELS = Map.of(
            "NONE", "无",
            "HELD", "托管中",
            "RELEASED", "已释放",
            "FROZEN", "冻结"
    );

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderService orderService;

    /**
     * 执行 exportUsersExcel 相关逻辑。
     */
    @Override
    public byte[] exportUsersExcel() {
        List<User> users = userMapper.findAll();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("用户列表");
            CellStyle headerStyle = createHeaderStyle(workbook);
            String[] headers = {
                    "ID", "账号", "昵称", "角色", "邮箱", "造物币余额", "冻结余额",
                    "信用分", "评分", "评价数", "完成订单", "达人申请", "密码重置申请"
            };
            writeHeader(sheet, headers, headerStyle);

            int rowIdx = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;
                setCell(row, col++, user.getId());
                setCell(row, col++, user.getAccount());
                setCell(row, col++, user.getUsername());
                setCell(row, col++, labelRole(user.getRole()));
                setCell(row, col++, user.getEmail());
                setCell(row, col++, user.getZaowuBiBalance());
                setCell(row, col++, user.getFrozenBalance());
                setCell(row, col++, user.getCreditScore());
                setCell(row, col++, user.getRating());
                setCell(row, col++, user.getReviewCount());
                setCell(row, col++, user.getCompletedOrders());
                setCell(row, col++, labelArtisanApply(user.getArtisanApplyStatus()));
                setCell(row, col, labelPasswordReset(user.getPasswordResetStatus()));
            }
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("导出用户 Excel 失败", e);
        }
    }

    /**
     * 执行 exportOrdersExcel 相关逻辑。
     */
    @Override
    public byte[] exportOrdersExcel() {
        List<CustomOrder> orders = orderService.getAllOrders(null);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("订单列表");
            CellStyle headerStyle = createHeaderStyle(workbook);
            String[] headers = {
                    "订单ID", "作品标题", "类型", "买家", "手作人", "金额", "定金", "尾款",
                    "订单状态", "物流公司", "快递单号", "发货时间", "托管金额", "托管状态", "创建时间"
            };
            writeHeader(sheet, headers, headerStyle);

            int rowIdx = 1;
            for (CustomOrder order : orders) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;
                setCell(row, col++, order.getId());
                setCell(row, col++, order.getProductTitle());
                setCell(row, col++, labelProductType(order.getProductType()));
                setCell(row, col++, order.getBuyerName());
                setCell(row, col++, order.getArtisanName());
                setCell(row, col++, order.getPrice());
                setCell(row, col++, order.getDepositAmount());
                setCell(row, col++, order.getBalanceAmount());
                setCell(row, col++, labelOrderStatus(order.getStatus()));
                setCell(row, col++, order.getShippingCompany());
                setCell(row, col++, order.getTrackingNumber());
                setCell(row, col++, formatDateTime(order.getShippedAt()));
                setCell(row, col++, order.getEscrowAmount());
                setCell(row, col++, labelEscrowStatus(order.getEscrowStatus()));
                setCell(row, col, formatDateTime(order.getCreateTime()));
            }
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("导出订单 Excel 失败", e);
        }
    }

    /**
     * 创建管理端。
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * 执行 writeHeader 相关逻辑。
     */
    private void writeHeader(Sheet sheet, String[] headers, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * 执行 setCell 相关逻辑。
     */
    private void setCell(Row row, int col, Object value) {
        Cell cell = row.createCell(col);
        if (value == null) {
            cell.setBlank();
            return;
        }
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
            return;
        }
        cell.setCellValue(String.valueOf(value));
    }

    /**
     * 执行 autoSizeColumns 相关逻辑。
     */
    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(width + 512, 256 * 40));
        }
    }

    /**
     * 执行 labelRole 相关逻辑。
     */
    private String labelRole(String role) {
        if (role == null || role.isBlank()) return "—";
        return ROLE_LABELS.getOrDefault(role, role);
    }

    /**
     * 执行 labelArtisanApply 相关逻辑。
     */
    private String labelArtisanApply(String status) {
        if (status == null || "NONE".equals(status)) return "无";
        return switch (status) {
            case "PENDING" -> "待审核";
            case "APPROVED" -> "已通过";
            case "REJECTED" -> "已拒绝";
            default -> status;
        };
    }

    /**
     * 执行 labelPasswordReset 相关逻辑。
     */
    private String labelPasswordReset(String status) {
        if (status == null || "NONE".equals(status)) return "无";
        return "PENDING".equals(status) ? "待处理" : status;
    }

    /**
     * 执行 labelProductType 相关逻辑。
     */
    private String labelProductType(String type) {
        if ("READY_MADE".equals(type)) return "成品";
        if ("CUSTOMIZABLE".equals(type)) return "定制";
        return type != null ? type : "—";
    }

    /**
     * 执行 labelOrderStatus 相关逻辑。
     */
    private String labelOrderStatus(String status) {
        if (status == null) return "—";
        return ORDER_STATUS_LABELS.getOrDefault(status, status);
    }

    /**
     * 执行 labelEscrowStatus 相关逻辑。
     */
    private String labelEscrowStatus(String status) {
        if (status == null) return "—";
        return ESCROW_STATUS_LABELS.getOrDefault(status, status);
    }

    /**
     * 执行 formatDateTime 相关逻辑。
     */
    private String formatDateTime(LocalDateTime time) {
        return time != null ? time.format(DT_FMT) : "—";
    }
}
