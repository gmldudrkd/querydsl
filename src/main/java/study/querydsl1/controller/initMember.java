package study.querydsl1.controller;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl1.entity.Member;
import study.querydsl1.entity.Team;

@Profile("local") //local 프로파일을 사용 ( application.yml 설정 )
@Component //스프링 빈에 자동 등록
@RequiredArgsConstructor
public class initMember {

    private final InitMemberService initMemberService;
    @PostConstruct
    public void init() {
        //스프링을 띄우면 프로파일 Local을 실행하고 PostConstruct를 실행 -> initMemberService가 실행됨
        initMemberService.init();
    }

    @Component
    static class InitMemberService {
        @PersistenceContext
        private EntityManager em;

        @Transactional
        public void init() { //데이터 초기화
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");
            em.persist(teamA);
            em.persist(teamB);

            for (int i = 0; i < 100; i++) {
                Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                em.persist(new Member("member"+i, i , selectedTeam));
            }
        }
    }

}
