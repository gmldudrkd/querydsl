package study.querydsl1.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl1.dto.MemberSearchCondition;
import study.querydsl1.dto.MemberTeamDto;
import study.querydsl1.entity.Member;
import study.querydsl1.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

//스프링데이터 Jpa 가 제공하는 기능을 사용한 레포지토리
@SpringBootTest
@Transactional
class MemberRepositoryTest {
    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void basicTest() {
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();
        //org.assertj.core.api.Assertions.assertThat(findMember).isEqualTo(member);
        assertThat(findMember).isEqualTo(member);

        //List<Member> result1 = memberJpaRepository.findAll();
        List<Member> result1 = memberRepository.findAll();
        assertThat(result1).containsExactly(member); //결과가 member를 가지고 있냐?

        //List<Member> result2 = memberJpaRepository.findByUsername("member1");
        List<Member> result2 = memberRepository.findByusername("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    public void querydslTest() {
        Team team1 = new Team("teamA");
        Team team2 = new Team("teamB");
        em.persist(team1);
        em.persist(team2);

        Member member1 = new Member("member1", 10, team1);
        Member member2 = new Member("member2", 20, team1);

        Member member3 = new Member("member3", 30, team2);
        Member member4 = new Member("member4", 40, team2);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");
        //동적쿼리에서 조건이 다 빠지게되면 (위의 조건이 아무것도 없으면) 전체 테이블 데이터를 조회해온다!
        //동적쿼리 작성 시에는 Limit 를 걸든지 default 가 있어야한다!!

        List<MemberTeamDto> result = memberRepository.search(condition);
        assertThat(result).extracting("username").containsExactly("member4");
    }

    @Test
    public void searchPageSimple() {
        Team team1 = new Team("teamA");
        Team team2 = new Team("teamB");
        em.persist(team1);
        em.persist(team2);

        Member member1 = new Member("member1", 10, team1);
        Member member2 = new Member("member2", 20, team1);

        Member member3 = new Member("member3", 30, team2);
        Member member4 = new Member("member4", 40, team2);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest =  PageRequest.of(0,3); //0 page / 3count

        Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition,pageRequest);
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("username").containsExactly("member1", "member2", "member3");
    }
}