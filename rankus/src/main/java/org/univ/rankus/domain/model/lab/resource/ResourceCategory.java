package org.univ.rankus.domain.model.lab.resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 랩실 자료 카테고리
 */
@Getter
@RequiredArgsConstructor
public enum ResourceCategory {

    LECTURE_NOTE("강의자료"),
    ASSIGNMENT("과제"),
    RESEARCH("연구자료"),
    REFERENCE("참고자료"),
    DOCUMENT("문서"),
    SOFTWARE("소프트웨어"),
    OTHER("기타");

    private final String displayName;
}