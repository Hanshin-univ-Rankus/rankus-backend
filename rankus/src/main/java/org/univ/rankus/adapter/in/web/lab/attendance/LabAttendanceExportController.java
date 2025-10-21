package org.univ.rankus.adapter.in.web.lab.attendance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.univ.rankus.application.port.in.lab.export.ExportLabAttendanceQuery;
import org.univ.rankus.common.security.customUser.CustomUserDetails;

import java.time.LocalDate;

/**
 * 랩실 출석 Excel 내보내기 컨트롤러
 */
@RestController
@RequestMapping("/api/labs/{labId}/attendance")
@RequiredArgsConstructor
@Tag(name = "Lab Attendance Export", description = "랩실 출석 Excel 내보내기 API")
public class LabAttendanceExportController {

    private final ExportLabAttendanceQuery exportLabAttendanceQuery;

    @Operation(summary = "랩실 출석 데이터 Excel 다운로드", description = "랩실의 모든 출석 데이터를 Excel 파일로 다운로드합니다.")
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labMemberPermissionEvaluator.canViewLabMembers(#labId, authentication)")
    public ResponseEntity<byte[]> exportLabAttendance(
            @Parameter(description = "랩실 ID") @PathVariable Long labId,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long requesterId = userDetails.getUserId();

        byte[] excelData = exportLabAttendanceQuery.exportLabAttendanceToExcel(labId, requesterId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "lab_attendance_" + labId + "_" + LocalDate.now() + ".xlsx");
        headers.setContentLength(excelData.length);

        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }

    @Operation(summary = "랩실 출석 통계 Excel 다운로드", description = "특정 기간의 랩실 출석 통계를 Excel 파일로 다운로드합니다.")
    @GetMapping("/export/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labMemberPermissionEvaluator.canViewLabMembers(#labId, authentication)")
    public ResponseEntity<byte[]> exportLabAttendanceStatistics(
            @Parameter(description = "랩실 ID") @PathVariable Long labId,
            @Parameter(description = "시작 날짜") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "종료 날짜") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long requesterId = userDetails.getUserId();

        byte[] excelData = exportLabAttendanceQuery.exportLabAttendanceStatistics(labId, fromDate, toDate, requesterId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "lab_attendance_statistics_" + labId + "_" + fromDate + "_to_" + toDate + ".xlsx");
        headers.setContentLength(excelData.length);

        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }

    @Operation(summary = "멤버 출석 기록 Excel 다운로드", description = "특정 멤버의 출석 기록을 Excel 파일로 다운로드합니다.")
    @GetMapping("/export/member/{memberId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labMemberPermissionEvaluator.canViewLabMembers(#labId, authentication)")
    public ResponseEntity<byte[]> exportMemberAttendanceHistory(
            @Parameter(description = "랩실 ID") @PathVariable Long labId,
            @Parameter(description = "멤버 ID") @PathVariable Long memberId,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long requesterId = userDetails.getUserId();

        byte[] excelData = exportLabAttendanceQuery.exportMemberAttendanceHistory(labId, memberId, requesterId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "member_attendance_" + memberId + "_" + LocalDate.now() + ".xlsx");
        headers.setContentLength(excelData.length);

        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }

    @Operation(summary = "출석 증명서 Excel 생성", description = "특정 멤버의 출석 증명서를 Excel 파일로 생성합니다.")
    @GetMapping("/export/certificate/{memberId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSOR') or @labMemberPermissionEvaluator.canViewLabMembers(#labId, authentication)")
    public ResponseEntity<byte[]> generateAttendanceCertificate(
            @Parameter(description = "랩실 ID") @PathVariable Long labId,
            @Parameter(description = "멤버 ID") @PathVariable Long memberId,
            @Parameter(description = "증명 시작 날짜") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "증명 종료 날짜") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long requesterId = userDetails.getUserId();

        byte[] excelData = exportLabAttendanceQuery.generateAttendanceCertificate(labId, memberId, fromDate, toDate, requesterId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "attendance_certificate_" + memberId + "_" + fromDate + "_to_" + toDate + ".xlsx");
        headers.setContentLength(excelData.length);

        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }
}