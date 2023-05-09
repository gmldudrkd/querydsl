package study.querydsl1.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // protected 타입인 기본생성자를 생성해주는 어노테이션
@ToString(of = {"id", "username", "age"}) //아래 주석친 내용 생성, 연관관계 매핑값은 안넣는게 좋다 (무한루프돔)
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public Member(String username) {
        this(username, 0);
    }

    public Member(String username, int age) {
        this(username, age, null);
    }

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if (team != null){
            chageTeam(team);
        }
    }

    public void chageTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }

//    @Override
//    public String toString() {
//        return "Member{" +
//                "id=" + id +
//                ", username='" + username + '\'' +
//                ", age=" + age +
//                ", team=" + team +
//                '}';
//    }
}
