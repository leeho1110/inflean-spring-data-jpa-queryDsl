package study.datajpa.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.datajpa.entity.Member;
import study.datajpa.entity.QMember;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static study.datajpa.entity.QMember.member;
import static study.datajpa.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QueryDslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void setUp(){
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void caseJpql(){
        String qlString = "select m from Member m where m.username = :username";
        List<Member> resultList = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getResultList();
    }

    @Test
    public void caseQueryDSL(){
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember qMember = new QMember("memberDSL");

        Member member = queryFactory
                .select(qMember)
                .from(qMember)
                .where(qMember.username.eq("member1"))
                .fetchOne();

        System.out.println(member.toString());
    }

    @Test
    public void caseQueryDSL2(){
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(
                        member.username.eq("member1")
                        .and(member.age.gt(10))
                )
                .fetchOne();

    }

    @Test
    public void caseQueryDSL_queryResult(){
        QueryResults<Member> findMember = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .fetchResults();

        List<Member> results = findMember.getResults();
        long total = findMember.getTotal();

        System.out.println("total = " + total);
        for (Member result : results) {
            System.out.println("result = " + result.getUsername());
        }
    }

    @Test
    public void caseQueryDSL_paging(){
        List<Member> findMember = queryFactory
                .selectFrom(member)
                .offset(3) // 시작하는 index
                .limit(5) // 최대 n건 조회
                .orderBy(member.username.desc())
                .fetch();

        int size = findMember.size();
        System.out.println("size = " + size);
    }

    @Test
    public void caseQueryDSL_join(){
        List<Member> member = queryFactory
                .selectFrom(QMember.member)
                .join(QMember.member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        for (Member member1 : member) {
            String name = member1.getTeam().getName();
            System.out.println("name = " + name);

        }
    }

    @Test
    public void caseQueryDSL_fetch(){
        em.flush();
        em.clear();

        Member member = queryFactory
                .selectFrom(QMember.member)
                .join(QMember.member.team, team).fetchJoin()
                .where(QMember.member.username.eq("member1"))
                .fetchOne();
    }

    @Test
    public void caseQueryDSL_subQuery(){
        QMember sub = new QMember("sub");

        Member member = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.age.in(
                        select(sub.age.max())
                                .from(sub)
                )).fetchOne();

        System.out.println(member.getAge());

        List<String> of = List.of();
    }
}
