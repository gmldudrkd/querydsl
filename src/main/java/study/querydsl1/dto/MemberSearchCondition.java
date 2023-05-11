package study.querydsl1.dto;

import lombok.Data;

@Data
public class MemberSearchCondition {
    //조건을 넣었을때 검색하는 기능 클래스 > 아래 값이 조건내용
    private String username;
    private String teamName;
    private Integer ageGoe; //integer: 값이 null 일수잇다 / 나이가 크거나 같
    private Integer ageLoe; //나이가 작거나 같
}
