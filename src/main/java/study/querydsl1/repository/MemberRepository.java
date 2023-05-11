package study.querydsl1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.querydsl1.entity.Member;

import java.util.List;

//스프링데이터 Jpa로 변경
public interface MemberRepository extends JpaRepository<Member, Long> , MemberRepositoryCustom{
    //스프링데이터 정의 레포지토리 인터페이스 말고 조금 복잡한 구현체는 직접 구현해서 사용 : 사용자 정의 레포지토리(MemberRepositoryCustom)

    //기본내장을 제외한 메서드
    List<Member> findByusername(String username); //스프링데이터 Jpa는 메서드이름으로 자동으로 where 쿼리 생성
    //select m from Member m where m.username=?
}
