package org.univ.rankus.common.security.permission;

/**
 * 권한 문자열 상수 모음. 컨트롤러의 SpEL 문자열에는 영향을 주지 않지만,
 * 서버 코드 내 오타를 방지하고 일관성을 높이기 위해 사용합니다.
 */
public final class PermissionConstants {
    private PermissionConstants() {}

    // Core
    public static final String VIEW = "VIEW";
    public static final String CREATE = "CREATE";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String MANAGE = "MANAGE";

    // Lab Notice
    public static final String VIEW_NOTICES = "VIEW_NOTICES";
    public static final String MANAGE_NOTICES = "MANAGE_NOTICES";

    // Attendance
    public static final String VIEW_ATTENDANCE = "VIEW_ATTENDANCE";
    public static final String MANAGE_ATTENDANCE = "MANAGE_ATTENDANCE";

    // Vote
    public static final String VIEW_VOTES = "VIEW_VOTES";
    public static final String CREATE_VOTE = "CREATE_VOTE";
    public static final String PARTICIPATE = "PARTICIPATE";
    public static final String VIEW_RESULTS = "VIEW_RESULTS";

    // Calendar
    public static final String MANAGE_CALENDAR = "MANAGE_CALENDAR";
    public static final String VIEW_CALENDAR = "VIEW_CALENDAR";
    public static final String VIEW_APPLICANTS = "VIEW_APPLICANTS";

    // Interview
    public static final String MANAGE_INTERVIEWS = "MANAGE_INTERVIEWS";
    public static final String VIEW_INTERVIEWS = "VIEW_INTERVIEWS";

    // Lab Application
    public static final String APPROVE = "APPROVE";
    public static final String REJECT = "REJECT";
    public static final String CANCEL = "CANCEL";
}

