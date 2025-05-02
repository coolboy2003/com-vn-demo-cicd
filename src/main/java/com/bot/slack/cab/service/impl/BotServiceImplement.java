package com.bot.slack.cab.service.impl;

import com.bot.slack.cab.helper.TimeUtil;
import com.bot.slack.cab.model.BotReq;
import com.bot.slack.cab.model.ServiceDeployment;
import com.bot.slack.cab.service.BotService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BotServiceImplement implements BotService  {
    private static final String VAL_DESCRIPTION = "N/A"; // Hoặc để trống nếu muốn
    private static final String VAL_DIFF_LINKS = "N/A";
    private static final String VAL_RELATED_TICKET = "N/A";
    private static final String VAL_VERIFIED_ON = "SIT"; // Theo ảnh
    private static final String VAL_IMPACT_TO_FUNCTION = "N/A"; // Theo ảnh
    private static final String VAL_CHANGE_TYPE = "Normal"; // Theo ảnh
    private static final String VAL_PIPELINE_LINK = "N/A";
    private static final String VAL_EXECUTION_ID = "N/A";
    private static final String VAL_IMPACT_LEVEL = "Low";   // Theo ảnh
    private static final String VAL_TEST_PLAN = "N/A";
    private static final String VAL_PLANNED_START_END = String.valueOf(TimeUtil.getPlannedStartAndEnd());
    private static final String NOT_AVAILABLE = "N/A";
    // private static final Pattern LAST_NUMBER_PATTERN = Pattern.compile("\\.(\\d+)$");
    /**
     * Định dạng đối tượng BotReq (phiên bản giới hạn) thành chuỗi theo mẫu.
     * @param request Đối tượng BotReq chỉ chứa summary, deploymentServices, ticket.
     * @return Chuỗi đã được định dạng.
     */

    public String calculatePreviousVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            return null; // Không có phiên bản để tính toán
        }

        String[] parts = version.split("\\."); // Tách version thành các phần (major.minor.patch)
        if (parts.length == 0) {
            return null;
        }

        try {
            // Duyệt ngược từ phần cuối cùng (patch) để giảm dần
            for (int i = parts.length - 1; i >= 0; i--) {
                int number = Integer.parseInt(parts[i]);
                if (number > 0) {
                    parts[i] = String.valueOf(number - 1); // Giảm số hiện tại
                    // Đặt tất cả các phần bên phải thành giá trị tối đa (9)
                    for (int j = i + 1; j < parts.length; j++) {
                        parts[j] = "9";
                    }
                    return String.join(".", parts); // Gộp lại thành phiên bản mới
                }
            }
        } catch (NumberFormatException e) {
            return null; // Trường hợp không hợp lệ
        }

        // Nếu tất cả các số đều là 0, không thể giảm thêm
        return null;
    }

    @Override
    public String format(BotReq req) {
        if (req == null) {
            return "Error: Request object is null.";
        }

        StringBuilder sb = new StringBuilder();
        List<ServiceDeployment> services = req.getDeploymentServices();

        // Sử dụng các trường từ BotReq
        appendField(sb, "Summary", req.getSummary());
        appendField(sb, "Ticket", req.getTicket()); // Trường ticket từ req

        // Thêm các trường cố định/mặc định
        appendField(sb, "Description", VAL_DESCRIPTION);
        appendField(sb, "Diff links", VAL_DIFF_LINKS);
        appendField(sb, "Related Ticket", VAL_RELATED_TICKET);
        appendField(sb, "Verified on", VAL_VERIFIED_ON);
        appendField(sb, "Impact to Function", VAL_IMPACT_TO_FUNCTION);
        appendField(sb, "Change Type", VAL_CHANGE_TYPE);
        appendField(sb, "Pipeline Link", VAL_PIPELINE_LINK);
        appendField(sb, "Execution ID", VAL_EXECUTION_ID);

        // Affected Services (từ deploymentServices trong req)
        sb.append("\n\n• Affected Services:");
        if (services != null && !services.isEmpty()) {
            boolean hasAffectedServices = false;
            for (ServiceDeployment service : services) {
                String repoName = service.getRepoName();
                if (repoName != null && !repoName.trim().isEmpty()) {
                    sb.append("\n  ◦ ").append(repoName);
                    hasAffectedServices = true;
                }
            }
            if (!hasAffectedServices) sb.append(" ").append(NOT_AVAILABLE);
        } else {
            sb.append(" ").append(NOT_AVAILABLE);
        }

        appendField(sb, "Impact Level", VAL_IMPACT_LEVEL); // Giá trị cố định

        // Implementation Plan (từ deploymentServices trong req)
        sb.append("\n\n• Implementation Plan:");
        if (services != null && !services.isEmpty()) {
            boolean hasImplPlan = false;
            for (ServiceDeployment service : services) {
                if (isValidServiceForPlan(service)) {
                    sb.append("\n  ◦ Deploy the ").append(service.getRepoName())
                            .append(" version ").append(getValueOrNA(service.getVersion()));
                    hasImplPlan = true;
                }
            }
            if (!hasImplPlan) sb.append(" ").append(NOT_AVAILABLE);
        } else {
            sb.append(" ").append(NOT_AVAILABLE);
        }

        // Backout Plan (từ deploymentServices trong req)
        sb.append("\n\n• Backout Plan:");
        if (services != null && !services.isEmpty()) {
            boolean hasBackoutPlan = false;
            for (ServiceDeployment service : services) {
                if (isValidServiceForPlan(service)) {
                    // Sử dụng version đã có hoặc đã tính toán, nếu vẫn null thì dùng N/A
                    String displayRollbackVersion = getValueOrNA(service.getVersion());

                    sb.append("\n  ◦ Rollback the ").append(service.getRepoName())
                            .append(" version ").append(calculatePreviousVersion(displayRollbackVersion));
                    hasBackoutPlan = true;
                }
            }
            if (!hasBackoutPlan) sb.append(" ").append(NOT_AVAILABLE);
        } else {
            sb.append(" ").append(NOT_AVAILABLE);
        }

        // Các trường cố định còn lại
        appendField(sb, "\n\nTest Plan", VAL_TEST_PLAN); // Thêm dòng trống trước

        sb.append("\n\n• Planned start - Planned end:"); // Thêm dòng trống
        sb.append("\n  ◦ ").append(VAL_PLANNED_START_END); // Hiển thị với định dạng có dấu "◦"

        // Xóa ký tự thừa ở đầu nếu có
        if (sb.length() > 0 && sb.charAt(0) == '\n') {
            sb.deleteCharAt(0);
        }
        // Đảm bảo dòng đầu tiên bắt đầu bằng '•' nếu có nội dung
        if (sb.length() > 0 && sb.charAt(0) != '•' && sb.charAt(0) != '\n') {
            sb.insert(0, "• ");
        } else if (sb.length() > 0 && sb.charAt(0) == '\n' && sb.charAt(1) != '•') {
            // Trường hợp có dòng trống ở đầu, chèn '• ' vào dòng thứ 2
            int firstContentIndex = -1;
            for (int i = 0; i < sb.length(); i++) {
                if (!Character.isWhitespace(sb.charAt(i))) {
                    firstContentIndex = i;
                    break;
                }
            }
            if (firstContentIndex != -1 && sb.charAt(firstContentIndex) != '•') {
                sb.insert(firstContentIndex, "• ");
            }
        }


        return sb.toString();
    }

    // --- Phương thức hỗ trợ (giữ nguyên) ---

    public boolean isValidServiceForPlan(ServiceDeployment service) {
        return service != null && service.getRepoName() != null && !service.getRepoName().trim().isEmpty();
    }

    public String getValueOrNA(String value) {
        return (value != null && !value.trim().isEmpty()) ? value : NOT_AVAILABLE;
    }

    public void appendField(StringBuilder sb, String label, String value) {
        String displayValue = getValueOrNA(value);

        if (sb.length() > 0 && !label.startsWith("\n")) {
            sb.append("\n");
        }
        if (!label.startsWith("\n")) {
            sb.append("• ");
        } else {
            sb.append(label.startsWith("\n\n") ? "\n\n• " : "\n• ");
            label = label.trim();
        }

        sb.append(label).append(": ").append(displayValue);
    }
}
