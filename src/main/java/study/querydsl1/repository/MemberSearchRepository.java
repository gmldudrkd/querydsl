package study.querydsl1.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import study.querydsl1.dto.MemberSearchCondition;
import study.querydsl1.dto.MemberTeamDto;
import study.querydsl1.dto.QMemberTeamDto;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl1.entity.QMember.member;
import static study.querydsl1.entity.QTeam.team;

@Repository
public class MemberSearchRepository {

    private JPAQueryFactory queryFactory;
    private EntityManager em;

    public MemberSearchRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

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
