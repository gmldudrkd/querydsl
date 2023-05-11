package study.querydsl1.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl1.dto.MemberSearchCondition;
import study.querydsl1.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {
    //사용자 정의 인터페이스 구현
    //특정 api 에 종속되거나 그럴경우 cusom으로 안빼고 조회용 repo를 만드는 것도 방법, 무조건 custom 에 넣을 필욘없다.
    List<MemberTeamDto> search(MemberSearchCondition condition);

    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable); //단순한쿼리
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);// 카운트쿼리 와 아닌 것 분리
}
