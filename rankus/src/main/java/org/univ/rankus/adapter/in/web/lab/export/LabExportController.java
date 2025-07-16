package org.univ.rankus.adapter.in.web.lab.export;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.univ.rankus.application.port.in.lab.export.ExportLabAttendanceQuery;
import org.univ.rankus.application.port.in.lab.export.ExportLabAttendanceQuery.ExportLabAttendanceCommand;
import org.univ.rankus.common.security.customUser.CustomUserDetails;

/**
 * 랩실 내보내기 컨트롤러
 */
@RestController
@RequestMapping("/api/labs/{labId}/export")
@RequiredArgsConstructor
@Tag(name = "Lab Export", description = "랩실 내보내기 API")
public class LabExportController {

    private final ExportLabAttendanceQuery exportLabAttendanceQuery;

    @Operation(summary = "랩실 출석 Excel 내보내기", description = "랩실 출석 데이터를 Excel 파일로 내보냅니다.")
    @GetMapping("/attendance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or hasRole('LAB_LEADER') or hasRole('LAB_MANAGER')")
    public ResponseEntity<byte[]> exportLabAttendance(
            @Parameter(description = "랩실 ID") @PathVariable Long labId,
            Authentication authentication) {

        // 안전한 인증 정보 추출
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        CustomUserDetails userDetails = (CustomUserDetails) principal;
        Long requesterId = userDetails.getUserId();

        ExportLabAttendanceCommand command = new ExportLabAttendanceCommand(labId, requesterId);
        byte[] excelData = exportLabAttendanceQuery.exportLabAttendance(command);

        // null 체크 추가
        if (excelData == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "lab_attendance_" + labId + ".xlsx");

        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }
}