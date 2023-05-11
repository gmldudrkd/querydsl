package study.querydsl1.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl1.dto.*;
import study.querydsl1.dto.QMemberDto;
import study.querydsl1.dto.QMemberTeamDto;
import study.querydsl1.entity.Member;
import study.querydsl1.entity.QMember;
import study.querydsl1.entity.QTeam;

import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.isEmpty;
import static study.querydsl1.entity.QMember.*;
import static study.querydsl1.entity.QTeam.team;

@Repository
public class MemberJpaRepository {
    private final EntityManager em; //엔티티매니저(프록시로 주입, 진짜 영속성 컨텍스트 아님)는 트랜잭션단위로 다른곳에 바인딩되도록 라우팅해주기 때문에 멀티스레드 환경에서도 데이터가 겹칠일 없다.
    private final JPAQueryFactory queryFactory; //querydsl 사용하기 위함, 쿼리팩토리는 엔티티매니저에 의존하기 때문에 동일하게 멀티스레드 환경에서도 문제없다.

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em); //파라미터로 엔티티매니저 필요
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findAll_querydsl() {
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<Member> findusername_querydsl(String username) {
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition memberSearchCondition){

        BooleanBuilder builder = new BooleanBuilder();
        //if(StringUtils.hasText(memberSearchCondition.getUsername())){ // null or "" 값이 아닐경우 잡아주는 라이브러리
        if(hasText(memberSearchCondition.getUsername())){
            builder.and(member.username.eq(memberSearchCondition.getUsername()));
        }
        if (hasText(memberSearchCondition.getTeamName())) {
            builder.and(team.name.eq(memberSearchCondition.getTeamName()));
        }
        if (memberSearchCondition.getAgeGoe() != null) {
            builder.and(member.age.goe(memberSearchCondition.getAgeGoe()));
        }
        if (memberSearchCondition.getAgeLoe() != null) {
            builder.and(member.age.loe(memberSearchCondition.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teanId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
    }

    //builder 보다 where 절을 사용하는게 훨씬 가독성이 좋고, 재사용이 가능하다.
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teanId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();
    }

    private BooleanExpression ageBetween(int ageGoe, int ageLoe) {
        //이런식으로 조립이 가능하나 Null 값조심!!!
        return ageGoe(ageGoe).and(ageLoe(ageLoe));
    }

    //private Predicate ageLoe(Integer ageLoe) {
    private BooleanExpression ageLoe(Integer ageLoe) { //predicate 라는 인터페이스보다 booleanexpression 타입이 추후 조건절 조립할때 용이함
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }
}
