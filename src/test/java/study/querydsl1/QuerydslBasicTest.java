package study.querydsl1;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl1.dto.MemberDto;
import study.querydsl1.dto.QMemberDto;
import study.querydsl1.dto.UserDto;
import study.querydsl1.entity.Member;
import study.querydsl1.entity.QMember;
import study.querydsl1.entity.QTeam;
import study.querydsl1.entity.Team;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.*;
import static study.querydsl1.entity.QMember.member;
import static study.querydsl1.entity.QTeam.team;


/*
member.username.eq("member1") // username = 'member1'
member.username.ne("member1") //username != 'member1'
member.username.eq("member1").not() // username != 'member1'
member.username.isNotNull() //이름이 is not null
member.age.in(10, 20) // age in (10,20)
member.age.notIn(10, 20) // age not in (10, 20)
member.age.between(10,30) //between 10, 30
member.age.goe(30) // age >= 30
member.age.gt(30) // age > 30
member.age.loe(30) // age <= 30
member.age.lt(30) // age < 30
member.username.like("member%") //like 검색
member.username.contains("member") // like ‘%member%’ 검색
member.username.startsWith("member") //like ‘member%’ 검색
*/

@SpringBootTest
@Transactional
@Commit
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;
    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);

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
    }

    @Test
    public void startJpql() {
        //search member1
        Member findMember = em.createQuery("select m from Member m where m.username=:username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
        //QMember m = new QMember("m"); //qmember의 별칭, 같은 테이블을 조인하는 등,, 할때 사용
//        QMember m = QMember.member;
//
//        Member findmember = queryFactory.select(m)
//                .from(m)
//                .where(m.username.eq("member1"))
//                .fetchOne();

        //위 주석내용을 아래와같이 줄일 수 있다. member -> QMember.member 를 옵션+엔터 로 static import 처리 한것!
        Member findmember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findmember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndparam() {
        //where 절, 처리
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resutlFetch() {
//        List<Member> fetch = queryFactory
//                .selectFrom(member)
//                .fetch();
//
//        Member fetchOne = queryFactory
//                .selectFrom(member)
//                .fetchOne();
//
//        Member fetchFirst = queryFactory
//                .selectFrom(member)
//                .fetchFirst();//Limit 1

        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults(); //fetchResults 내용의 내용을 가져와야함

        long total = results.getTotal(); //페이징을 위한 토탈카운트
        List<Member> content = results.getResults();
    }

    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast()) //NULL인 값은 마지막!
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member member7 = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(member7.getUsername()).isNull();
    }

    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2() {
        QueryResults<Member> memberQueryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(memberQueryResults.getTotal()).isEqualTo(4);
        assertThat(memberQueryResults.getLimit()).isEqualTo(2);
        assertThat(memberQueryResults.getOffset()).isEqualTo(1);
        assertThat(memberQueryResults.getResults().size()).isEqualTo(2);
    }

    @Test
    public void aggregation() throws Exception{
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch(); //다양한 자료형이 있을 경우 tuple 사용, 실무에선 많이 사용안하고 dto로 적용

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
    }

    @Test
    public void group() throws Exception {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
    }

    /*teamA 에 소속된 모든직원*/
    @Test
    public void join() throws Exception{
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /*세타 조인 연관관계가 없는 테이블을 조인한다, - 디비에서 최적화함*/
    public void theta_join() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team) //member, team의 테이블의 데이터를 모두가져와서 비교
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /*teamA 인 팀에있는 멤버*/
    @Test
    public void join_on_filter() throws Exception {
        queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA")) //member.team, team이 id 로 매칭진행
                .fetch();
    }

    /*연관관계 없는 엔티티 외부조인*/
    @Test
    public void join_on_no_relation() throws Exception{
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name)) // leftJoin뒤에 team만 존재 (일반 Join은 2개), 한개일 경우 Id 안찾고 On절에 맞춰 조회함
                .fetch();

        for ( Tuple tuple :result){
            System.out.println("tuple = " + tuple);
        }
    }

//    @PersistenceContext
//    EntityManagerFactory emf; //엔티티매니저를 만드는 팩토리
//
//    @Test
//    public void fetchJoinUse() throws Exception{
//        em.flush();
//        em.clear();
//
//        Member result = queryFactory
//                .selectFrom(member)
//                .join(member.team, team).fetchJoin()
//                .where(member.username.eq("member1"))
//                .fetchOne();
//
//        // join 의 경우 From대상 엔티티만 영속성 컨텍스트에 담는다 ! > 단순히 from의 데이터만 조회할 경우 필요!
//        // fetch join 의 경우 조인된 엔티티도 모두 영속성 컨텍스트에 담는다 > 조인에 걸린 엔티티도 필요할 경우 사용!! > 1+N 문제해결!
//
//        boolean load = emf.getPersistenceUnitUtil().isLoaded(result.getTeam()); //엔티티매니저에 로드되어있는 엔티티인지 확인
//        assertThat(load).as("패치조인 적용").isTrue();
//    }

    /*나이가 가장많은 회원조회 - 서브쿼리*/
    @Test
    public void subQuery() throws Exception{
        QMember membersub = new QMember("membersub"); //동일한 엔티티가 서브쿼리에 들어가므로 as(alianse?) 가 달라야함

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(membersub.age.max())
                                .from(membersub)
                ))
                .fetch();

        assertThat(result).extracting("age").containsExactly(40);
    }

    /*나이가 평균이상인 회원 - 서브쿼리*/
    @Test
    public void subqueryGoe() {
        QMember membersub = new QMember("membersub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(
                        member.age.goe( //goe : 오른쪽 보다 크거나 같은
                                select(membersub.age.avg())
                                        .from(membersub)
                        )
                )
                .fetch();

        assertThat(result).extracting("age").containsExactly(30,40);
    }

    //select 절에 subquery
    @Test
    public void subQuerySelect() {
        QMember membersub = new QMember("membersub");

        List<Tuple> result = queryFactory
                .select(member.username,
                        select(membersub.age.avg()).from(membersub)
                )
                .from(member)
                .fetch();

        //JPAExpressions .select(membersub.age.avg()).from(membersub) > static import

        for (Tuple tuple : result){
            System.out.println("tuple.get(member.username) = " + tuple.get(member.username));
            System.out.println("tuple.get(JPAExpressions.select(membersub.age.avg()).from(membersub))  = " + tuple.get(select(membersub.age.avg()).from(membersub)) );
        }
    }
    //jpa jpql서브쿼리의 한계점은 from절의 서브쿼리를 지원하지 않는다! (querydsl도 jpql의 구현체이기 때문에 당연히 안됨)
    //해결법 : 서브쿼리를 조인으로 바꿔라! ( 거의 대부분 가능) > app에서 쿼리를 2번으로 분리 > nativeSQL 을 사용.

    /*case 문*/
    @Test
    public void caseTest() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("10years")
                        .when(20).then("20years")
                        .otherwise("etc.."))
                .from(member)
                .fetch();
    }

    @Test
    public void concat() {
        //{username}_{age}
        List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue())) //age는 타입이 string이 아니라 치환필요!
                .from(member)
                .fetch();

        for (String s : result){
            System.out.println("s = " + s);
        }
    }

    /*프로젝션 : select 문에 어떤걸 조회할것인가 > 일부 값 조회 시 엔티티 사용하지 말기! dto생성해서 사용 > 어떻게 Dto를 조회할 것인가에 대한 예제*/
    @Test
    public void simpleProjection() {
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }
    
    // 튜플은 querydsl의 패키지 > 튜플은 리포지토리에서만 사용하도록 하자 , 추후 querydsl을 다른기술로 변경할때도 유용하고 앞단계층에 querydsl을 사용여부를 알려줄 필요없음
    // 서비스/ 컨트롤러로 던질때는 DTO 로 변환해서 전달!
    @Test
    public void tupleProjection() {
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            String name = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("name = " + name);
            System.out.println("age = " + age);
        }
    }

    //프로젝션 결과를 dto로 반환
    // 순수 jpa를 사용할 경우 생성자 New를 사용해서 조회
    @Test
    public void findDtoByJpa() {
        List<MemberDto> result = em.createQuery("select new study.querydsl1.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }


    //프로퍼티 접근방법
    @Test
    public void findDtoBySetter() {
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class, //빈으로 setter조회
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    //getter, setter 필요없이 값을 필드에 인젝션
    @Test
    public void findDtoByField() {
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    //생성자를 사용해서 노출, 대신에 타입만 잘맞춰주면 된다!!!
    @Test
    public void findDtoByConstructor() {
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findUserDto() {
        QMember membersub = new QMember("membersub");

        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class, //필드인젝션의 경우 이름매칭이 중요!
                        member.username.as("name"), //dto 와 엔티티명이 다를경우 as를 사용하여 맞춰준다
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(membersub.age.max())
                                        .from(membersub), "age"
                        ))) //서브쿼리로 값을 출력하고 싶을때, ExpressionUtils : 서브쿼리는 필드명이 없으니 age에 매칭
                .from(member)
                .fetch();
        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto); //가장큰 나이를 age에 동일하게 출력
        }
    }

    @Test
    public void findUserDtoByConstructor() {
        List<UserDto> result = queryFactory
                .select(Projections.constructor(UserDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }


    @Test
    public void findDtoByQfile() {
        // constructor(위) 와 동일하다고 생각할 수 있지만 생성자방법은 런타임오류를 뱉는다(실행 시 오류, 사용자가 클릭하면 오류발생)
        // 그러나, Dto를 Q파일로 생성한 해당방법(@QueryProjection) 은 컴파일 오류를 뱉는다(소스작성 시 오류) > 따라서 해당 방법이 더 좋음!
        // 다만 dto에 어노테이션이 적혀있어서 Querydsl에 의존적이게 된다, (변경할 경우 오류가 발생함), dto는 서비스, 컨트롤러에서 사용하는 거기 때문에 순수하게 가져가고 싶을경우 생성자 방법사용
        // Querydsl을 많이 사용하고 이정도는 가져가도 된다 싶으면 어노테이션 적용!
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    //동적쿼리
    @Test
    public void dynamicQuery_booleanBuilder() {
        //이름과 나이의 Null여부에 따라 동적으로 동작
        String usernameParam = "member1";
        Integer ageParam = null;

        List<Member> result = searchMemeber1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMemeber1(String usernameParam, Integer ageParam) {
        BooleanBuilder builder = new BooleanBuilder();
        // BooleanBuilder builder = new BooleanBuilder(member.username.eq(usernameParam)); //초기값을 넣을 수 잇다., 무조건 있다고 가정.

        //빌더에 where에 들어갈 문장을 조합
        if (usernameParam != null) {
            builder.and(member.username.eq(usernameParam));
        }
        if (ageParam != null) {
            builder.and(member.age.eq(ageParam));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    public void dynamicQuery_whereParam() {
        String usernameParam = "member2";
        Integer ageParam = 20;

        List<Member> result = searchMemeber2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMemeber2(String usernameParam, Integer ageParam) {
        return queryFactory
                .selectFrom(member)
                .where(allEq(usernameParam, ageParam))
                .fetch();
        ////.where(usernameEq(usernameParam), ageEq(ageParam)) //where 에 null 이 입력될 경우 무시됨.
    }

    private BooleanExpression ageEq(Integer ageParam) {
        return ageParam != null ? member.age.eq(ageParam) : null;
    }

    private BooleanExpression usernameEq(String usernameParam) {
        return usernameParam != null ? member.username.eq(usernameParam) : null;
    }

    private BooleanExpression allEq(String userCond, Integer ageCond) {
        return usernameEq(userCond).and(ageEq(ageCond));
    }

    //대량 수정,삭제 쿼리 > 벌크연산
    @Test
    public void bulkUpdate() {
        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();

        //영속성컨텍스트에는 기존 값으로 올라가 있다 > 벌크연산은 DB값을 바로 변경하기 때문에 영속성 컨텍스트와 값이 다르다.
        //셀렉트해서 가져오면 값이 영속성 컨텍스트의 값으로 유지된다. (우선권을 가짐)   > 그래서 아래와 같이 초기화!
        em.flush();
        em.clear();

        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();

        for (Member member1 : result){
            System.out.println("member1 = " + member1);
        }
    }

    @Test
    public void bulkAdd() {
        //모든회원의 나이를 1더하기
        long count = queryFactory
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();
    }

    @Test
    public void bulkDelete() {
        long count = queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();
    }

    //sql function 사용
    @Test
    public void sqlFunction() {
        //member 를 M 으로 변경하는 함수 > H2 Dialect 라는 클래스가 있어야함!!!
        List<String> result = queryFactory
                .select(Expressions.stringTemplate(
                        "function('replace', {0}, {1}, {2})",
                        member.username, "member", "M"
                ))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void sqlFunction2() {
        //일반적으로 디비에서 제공하는 함수는 내장되어 있음 무조건쓰기보다 찾아보자!
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .where(member.username.eq(member.username.lower()))
//                .where(member.username.eq(
//                        Expressions.stringTemplate("function('lower', {0})", member.username)
//                ))
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }


}
