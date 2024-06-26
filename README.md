# kyh_JPA
김영한의 JPA 로드맵 

### JPA 소개

- SQL 중점적인 개발의 문제점
    - Mapping 작업이 너무 오래걸린다.
    - 객체를 자바 컬렉션에 저장하듯이 저장할 수 없을까? → JPA
- JPA 소개
    - Java Persistence API
    - 자바 진영의 ORM 기술 표준
        
        *ORM? 객체와 관계형 DB와 매핑을 해주는 것
        
    - JPA는 애플리케이션과 JDBC 사이에서 동작
    - **패러다임 불일치 해결**
    - JPA가 만들어지기 전에 하이버네이트라는 오픈소스 ORM이 있었는데 이것을 본따 JPA를 만들었다.
- JPA를 사용하는 이유
    - SQL 중심적인 개발에서 객체 중심으로 개발
    - 생산성 : CRUD를 만들기 쉬움
    - 유지보수 : 필드 변경시 모든 SQL을 수정해야하는 문제를 해결
    - 패러다임 불일치 해결
        1. 상속
        저장 : 상속 관계로 있는 객체를 저장할 때 여러번 insert 코드를 작성하는 것이 아닌 .persist(object)의 하나의 코드로 알아서 insert 해줌
        조회 : 조인이 필요한 쿼리도 .find(object.class, id) 하나의 코드로 알아서 select 해줌
        2. 연관관계, 객체 그래프 탐색 : 연관 관계에 있는 테이블을 쉽게 저장하고 조회할 수 있음
        3. 신뢰할 수 있는 엔티티, 계층
        4. 동일한 트랜잭션에서 조회한 엔티티는 비교했을 때 같음을 보장함
            
            ```java
            Member member1 = jpa.find(Member.class, memberId);
            Member member2 = jpa.find(Member.class, memberId);
            member1 == member2; // true
            ```
            
    - 성능 최적화 기능
        1. 1차 캐시와 동일성 보장 : 같은 트랜잭션 안에서는 처음에는 sql을 날려서 반환하고 같은 엔티티를 조회했을 때 캐시된 엔티티를 들고옴(성능 향상)
        2. 트랜잭션을 지원하는 쓰기 지연
        INSERT : 트랜잭션을 커밋할 때까지 Insert Sql을 모았다가(bufferWirting) commit을 하는 순간 한번에 sql 실행
        3. 지연 로딩과 즉시 로딩 가능
        지연로딩: 객체가 실제 사용될 때 로딩
        즉시로딩: JOIN SQL로 한번에 연관된 객체까지 미리 조회

### JPA 설정하기

- 프로젝트 환경
    - 자바17
    - 하이버네이트 6
    - h2 2.2.224
    - maven
- persistance.xml 설정하기
    - resource/META-INF/persistance.xml을 생성
    - 설정 중 원래 javax의 패키지 이름을 jakarta로 변경해야함
    - propertie설정 중 hibernate.dialect 설정*

        ```java
        <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
        ```

      데이터 베이스의 종류에 따라 특정 함수가 존재하는데 이런 차이에 따른 종속성을 없애기 위해 위 처럼 설정함
      *h2를 사용하면 H2Dialect, mysql을 사용하면 MySQLDialect


### JPA 사용

```java
EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
EntityManager em = emf.createEntityManager();
```

- EntityManagerFactory : 어플리케이션이 실행될 때 한번만 생성해서 계속 사용한다.
- EntityManager: 한 요청에 한번 사용하고 버린다.(쓰레드에서 공유하면 x)
- **JPA를 사용하면서 모든 데이터 변경은 트랜젝션 안에서 실행되어야 한다!!**
- JPQL

    ```java
    List<Member> list = em.createQuery("SELECT m FROM Member AS m" ,Member.class)
                    .getResultList();
    ```

    - 직접 쿼리를 작성해서 조회
    - SQL과 JPQL의 차이점: SQL은 db의 테이블을 대상으로 쿼리 / JPQL은 객체를 대상으로 쿼리

## 영속성 컨텍스트

- JPA를 통해 DB에 데이터를 넣기전에 엔티티의 상태를 관리하는 장소

### 영속성 컨텍스트의 생명주기

1. 비영속(new/transient) - 영속성 컨텍스트와 전혀 상관없는 새로운 상태
    
    ```java
    // 생성만 한 상태
    Member m = new Member();
    m.setId(1);
    m.setName("jedong");
    ```
    
2. 영속(managed) - 영속성 컨텍스트에서 관리되고 있는 상태 → 조회, 등록 시 영속상태가 됨
    
    ```java
    // 영속성 컨텍스트에 담김
    em.persist(m)
    ```
    
3. 준영속(detached) - 영속성 컨텍스트에 저장되었다가 분리된 상태
4. 삭제(remove) - 삭제된 상태

### 영속성 컨텍스트 존재의 이점

1. 1차 캐시
    - 엔티티를 key와 value 형태로 영속성 컨텍스트에 저장을 해놓고 DB에 조회할 때 캐시에 해당 데이터가 있으면 DB에 요청하지 않고 캐시 데이터를 사용한다. → DB 호출 횟수를 줄인다.
    - 하지만 하나의 Transaction에서 사용되는 1차 캐시이기 때문에 큰 이점은 없다.
2. 영속 엔티티의 동일성을 보장
    - 1차 캐시로 저장하기 때문에 여러번 같은 엔티티를 조회하면 동일성을 보장한다.
        
        ```java
        Member m1 = em.find(Member.class, 101);
        Member m2 = em.find(Member.class, 101);
        
        System.out.println(m1 == m2); // true
        ```
        
3. 트랜젝션을 지원하는 쓰기 지연
    - 엔티티를 저장했을 때 바로 insert하지 않고 영속성 컨텍스트에 ‘1차 캐시’안에 넣고 ‘**쓰기SQL 저장소**’에  담아놓았다가 commit이 실행될 때 한번에 insert쿼리를 처리한다.
4. 엔티티 변경 감지(dirty checking)
    - 엔티티를 영속성 컨텍스트에 넣은 후 값을 변경하기만 하면 자바의 컬렉션처럼 그 값이 변경됨
    - 처음 엔티티가 영속성 컨텍스트에 담겼을 때 스냅샷을 찍어놓고 트랜젝션이 commit되는 시점에 변경을 감지해서 update문을 실행한다.

### Flush

- 영속성 컨텍스트의 저장된 데이터를 DB에 반영하는 것
- 영송성 컨텍스트를 flush하는 방법
    1. 직접 flush하기 ex.flush()
    2. 트랜젝션 커밋(자동 반영)
    3. JPQL 쿼리 수행
- flush가 실행될 때 영속성 컨텍스트가 비워지는 것이 아닌 해당 쿼리만 즉시 실행하는 것

## 엔티티 맵핑

### @Entity

- 이 어노테이션이 붙어야만 JPA에서 관리한다.
- **기본 생성자 필수**(protect, public으로 만들어야함)

### 기타 정보

- @Column 어노테이션의 속성은 ddl생성시에만 이용하고 런타임 중에는 영향을 주지 않는다.(속성으로 입력한 코드들은 ddl 생성할 때만 영향이 있다!)
## 필드와 컬럼 맵핑

### Entity 셋팅

1. **@Enumerated**
    - 자바의 Enum을 사용하고 싶을 때 Entity의 컬럼에 @Enumerated를 사용한다.
        
        ```java
        @Enumerated(EnumType.STRING)
        private RoleType roleType;
        ```
        
    - *JPA > Enum 활용법
        
        [Java Enum 활용기 | 우아한형제들 기술블로그](https://techblog.woowahan.com/2527/)
        
2. **@Temporal**
    - 일시 컬럼은 @Temporal을 사용한다.
        
        ```java
        @Temporal(TemporalType.TIMESTAMP)
        private Date createdDate;
        ```
        
    - 최신 하이버네이트에서는 이 어노테이션을 사용하지 않고 **LocalDate**, **LocalDateTime** 클래스를 사용해도 된다.
3. **@Lob**
    - varchar를 넘어서는 컨텐츠는 @Lob을 사용한다.
        
        ```java
        @Lob
        private String description;
        ```
        
4. @Transient
    - DB와 맵핑시키지 않고 자바에서만 사용하고 싶을 때

### @Column의 속성

1. insertable, updatable
    - 등록, 변경의 가능 여부
    - 기본은 true - 등록, 변경 시 해당 컬럼 변경됨
    - false로 셋팅하면 데이터 등록, 변경 시 db에 반영 안됨
    

### @Enumerated 주의점

- 속성에 ordinary를 사용하지 마라!
    - ordinary를 사용하게 되면 db에 enum의 값이 순서대로 숫자로 들어가기 때문에 나중에 enum에 값을 추가하면 어떤 값인지 알 수 없음
- 기본값이 ordinary이기 때문에 반드시 속성에 String으로 셋팅하자
    
    ```java
    @Enumerated(EnumType.STRING)
    private RoleType roleType;
    ```
    

### @GeneratedValue

**GenerationType.*IDENTITY의 특징***

- 이 속성은 db의 기본키 전략을 이용하기 때문에 JPA로 insert할 때 영속성 컨텍스트에 보관하지 않고 바로 db로 쿼리를 날린다.
    
    why? 직접 db에 날리지 않으면 현재 insert한 레코드의 id를 알 수 가 없다. 그래서  insert를 한 후 해당 id를 알기위해 바로 db에 날리는 것
    

**GenerationType.SEQUENCE 사용 시 최적화**

- 이 속성은 미리 DB에 시퀀스를 만들어 사용하는 속성이다.
- insert를 할 때 지금 사용해야할 id의 시퀀스를 가져오기 때문에 insert할 때마다 db 접속을 한다.
- 이런 성능 저하를 최적화 하기위해 **미리 특정 개수의 시퀀스를 미리 가지고 온다**.
    
    ```java
    @Entity
    @Table(name = "Member")
    @SequenceGenerator(
            name = "MEMBER_SEQ_GENERATOR",
            sequenceName = "MEMBER_SEQ",
            initialValue = 1, allocationSize = 50 // 1부터 시작해서 +50으로 늘린다. >> 이곳에서 50까지 미리 할당해 놓는다.
    )
    public class Member0505 {
    
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "MEMBER_SEQ_GENERATOR")
        private Long id;
    ```
    

*H2 연결 오류 해결

- 오류 : h2에서 새로운 db 생성이 안됨
- 해결 : 새로 생성할 때는 url을 jdbc:h2:~/jpashop 이런식으로 하고 이제 두번째부터는 jdbc:h2:tcp://[localhost/~/jpashop](http://localhost/~/jpashop) 이렇게 들어가야한다.

## 연관관계 매핑

### 객체를 테이블에 맞춰서 맵핑 했을 때 문제점

- 객체(Entity)를 테이블에 맞춰서 코드를 작성 했을 때!
    - 등록시 - 외래키 식별자를 직접 다룸 → 코드를 작성할 때 외래키가 있다면 직접 해당 외래키를 가지고 등록한다.
    - 조회시 - 외래키를 가진 객체 조회 후 다시 외래키로 한번 더 조회한다.
    - 둘 다 객체지향적인 방법이 아님

### 단방향 연관관계

(jpa-ex-0505 프로젝트 참고)

- 자바 코드에 Entity에 참조할 Entity를 작성하고 @ManyToOne (다 에서 1) 어노테이션을 붙인다.
    
    ```java
    @Entity
    public Member {
    	
    	@Id
    	private Long id;
    	
    	@ManyToOne
    	@JoinColumn(name = "TEAM_ID") // join할 때 필요한 컬럼 셋팅
    	private Team team;
    	
    }
    ```
    

### 양방향 연관관계와 연관관계 주인

(jpa-ex-0505 프로젝트 참고)

**양방향 연관관계?** 

- 테이블에서는 Member와 Team이 TEAM_ID라는 외래키로 양방향으로 연결되어있다. (TEAM_ID만 있으면 서로 연관을 맺을 수 있다.)
- 하지만 자바의 객체에서는 이렇게 양방향으로 연관을 맺으려면 서로 참조하는 필드를 만들어 줘야한다.
    
    ```java
    // Member.class
    @Entity
    public Member {
    		
    		......
    		
    		@ManyToOne
    		private Team team;
    		
    }
    
    // Team.class
    @Entity
    public Team {
    		.....
    		
    		@OneToMany(mappedBy = "team") // 이렇게 양방향으로 맵핑 셋팅
    		private List<Member> members = new ArrayList<>();
    		
    }
    ```
    
- 하지만 사실 객체에서 양방향을 설정했을 때 단방향으로 연결된 필드가 2개가 생기는 것이다.
그렇다면 JPA로 수정,등록을 할 때 Member의 team / Team의 Members 이 둘중 어떤 것을 참조해서 쿼리를 날려야 할까?
(Member의 team에 값을 변경해도 DB가 수정되고 Team의 Members의 값을 수정해도 DB에서 수정되는 오류가 발생함)
- 따라서 둘 중 한군데에서만 등록/수정을 처리해야한다. → **연관관계의 주인**을 설정해야한다.

**연관관계의 주인**

- 연관 관계의 주인을 설정하려면 주인이 아닌 필드에 mappedBy 속성을 설정해야한다.
    
    ```java
    @OneToMany(mappedBy = "team") // "Member의 team이라는 변수가 주인이다" 라는 설정
    private List<Member> members = new ArrayList<>();
    ```
    
- 이렇게 설정하면 연관관계의 주인인 Member의 team은 **등록/수정/조회**가 가능하고 Team의 members는 **조회**만 가능하다
- **누구를 주인으로 설정해야할까? → 외래키가 있는 객체를 주인으로 정하라**

**양방향 연관관계 사용시 주의점**

- 실제로 등록/수정되는 필드에 값을 넣어야 JPA에서 해당 쿼리가 동작한다. → 하지만 객체 지향적으로 사용하기 위해 mappedBy로 설정한 필드에도 값을 설정해 놓는다.
    
    ```java
    // Main.class
    Team t = new Team();
    t.setName("team1");
    em.persist(t);
    
    Member m = new Member();
    m.setName("member1");
    m.setTeam(t); // 실제 Member에 teamId가 맵핑되는 코드
    em.persist(m);
    
    t.getMembers().add(m); // 등록되는 쿼리가 실행되지는 않지만 객체 지향적으로 셋팅해주는 곳
    ```
    
    *t.getMembers().add(m); 코드의 이점
    
    - JPA의 구조 상 바로 insert되지않고 영속성 컨텍스트에 저장해놓기 때문에 위의 로직이 실행된 후 t.getMembers()의 값을 사용해야 한다면 반드시 add를 해놓아야한다.

**양뱡향 연관관계 사용 결론**

1. t.getMembers().add(m); 이런식으로 셋팅하는 것을 누락할 수 있기 때문에 **연관관계 편의 메서드**를 만들자
    
    ```java
    //Member.class
    public Member {
    		....
    		
    		public changeTeam(Team team) {
    				this.team = team;
    				
    				team.getMembers().add(this); // 이렇게 member(=this)를 넣는 코드를 추가한다. 
    		}		
    
    }
    ```
    
2. 양방향 매핑시 무한 루프를 조심해라! → toString, lombok의 toString, Json 생성라이브러리
    - lombok의 toString()은 쓰지마라!
    - Json 생성라이브러리 ex) Spring의 controller단 response → 응답을 Entity로 하지말고 Dto를 만들어서 반환해라
3. 단방향 매핑만으로도 이미 연관관계 매핑은 완료된 것이고 양방향 매핑은 객체 그래프 탐색 기능을 추가한 것 뿐이다.
따라서 단방향 매핑만 잘하고 그 후 개발하면서 필요할 때 양방향 매핑을 사용하라

> 연관관계 매핑 실전 예제 : jpa-ex-05062
>

### 일대다 [1:N]

- 보통 ‘다’가 주인인 연관관계를 맺는데 일대다 연관관계가 있을 수 있음
- 보통 다대일 연관관계를 많이 씀
- 다대일 연관관계보다 쿼리가 더 많이 나가는 단점이 있음(성능이슈) - 연관관계 관리를 위해 추가로 UPDATE SQL이 실행됨
- 실무에서는 유지보수를 위해 일대다 보다는 **다대일 양방향**을 쓰자

> 일대다 연관관계 매핑 예제 : jpa-ex-0508-member-team
>

## 일대일

> 일대일 예제 : jpa-ex-0511-member-locker
> 

### 일대일 매핑 방법

```java
// Member.class
@Entity
Member {
	.........
	
	@OneToOne
	@JoinColumn(name = "LOCKER_ID")
	private String Locker
}

// Locker.class
@Entity
Locker {
	.........
	
	@OneToOne(mappedBy = "locker")
	private String Locker
}
```

1. ManyToOne과 같이 외래키가 있는 엔티티에 @OneToOne과 @JoinColumn을 셋팅한다.
2. 양방향으로 쓰고싶다면 매칭된 엔티티에 @OneToOne과 속성으로 mappedBy를 추가한다.

### 일대일 연관관계일 때 DB에서 외래키의 위치

- 일대일 연관관계라면 외래키의 위치를 Member, Locker 테이블 둘 중 하나로 설정할 수 있음
    - ex) member_id를 Locker테이블의 외래키+유니크로 `or` locker_id를 Member테이블의 외래키+유니크 → 둘중 하나로 선택 가능
- 두 선택지는 장단점을 가지고 있음
    - Member(주 테이블)에 외래키를 놓을 경우
        - 장점: 주 테이블만 조회해도 대상 테이블(Locker)에 데이터가 있는지 확인 가능
        - 단점: 값이 없으면 외래키에 null이 허용됨
    - Locker(대상 테이블)에 외래키를 놓을 경우
        - 장점: 나중에 일대일관계에서 일대다로 바뀌었을 때 DB수정이 용이함
        - 단점: JPA의 지연 로딩을 사용할 수 없어서 즉시 로딩됨
- **결론: JPA를 사용할 때는 주테이블(Member)에 외래키를 놓는 것이 적절함 하지만 DB 측면에서는 애매할 수 있기 때문에 DBA와 적절한 대화가 필요**

## 다대다

- 결론적으로 사용하면 안됨
- @ManyToMany로 두 객체를 연결하면 자동으로 DB에 중간 테이블이 생성되지만 그 중간 테이블에 다른 컬럼을 설정할 수 없음
- **결론 : @ManyToMany를 사용하지 말고 실제로 중간 객체를 만들어서 일대다 다대일 관계로 만들어서 사용하라**
    
    <img width="578" alt="노션이미지01" src="https://github.com/YouJedong/kyh_JPA/assets/108327853/034d50fd-bb32-45d8-97a5-3e424218b9f9">
    
    - 중간 테이블에서 ORDER_ID로 PK를 둘 수도 있고 MEMBER_ID + PRODUCT_ID를 합쳐서 PK로 둘 수 있는데 JPA에서는 따로 ORDER_ID를 만들어서 사용하는 것을 **추천**

## 상속관계 맵핑

- 객체는 상속의 기능이 있지만 DB는 상속의 기능이 없다.
- 따라서 DB에서는 객체의 상속의 기능을 3가지 방식으로 만들 수 있다.

### 1. 조인 전략

<img width="372" alt="노션이미지02" src="https://github.com/YouJedong/kyh_JPA/assets/108327853/925a22a9-b43a-4409-8506-6b86961abbe3">

- 상위 테이블 1개와 하위 테이블 3개를 만드는 전략
- 부모 클래스에 @Inheritance로 설정한다.
    
    ```java
    // Item.class
    @Entity
    @Inheritance(strategy = InheritanceType.JOINED)
    abstract class Item {
    	.....
    }
    ```
    
    - 그럼 자식 클래스를 등록이나 조회를 했을 때 상위 테이블도 같이 insert, join한다.
- 하위 테이블의 값을 상위 테이블에 저장하고 싶다면 (DTYPE) @DiscriminatorColumn을 사용하면 컬럼이 자동 생성되고
자식 클래스의 이름으로 값이 들어간다.
    
    ```java
    @Entity
    @Inheritance(strategy = InheritanceType.JOINED)
    @DiscriminatorColumn
    public abstract class Item {}
    ```
    

### 2. 단일 테이블 전략

- 테이블 하나만 만들어서 모든 컬럼을 다 넣고 타입값으로 무슨 종류인지 구분하는 전략
- @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
- @DiscriminatorColumn이 없어도 dtype이 자동생성된다.

### 3. 구현 클래스마다 생성

- 테이블을 완전 나누는 전략 @Inheritance(strategy = InheritanceType.*TABLE_PER_CLASS*)
- @DiscriminatorColumn이 필요없다.
- 단점 - 부모 클래스로 조회할 때 따로 만들어진 테이블을 다 union all해서 가져온다.
    
    ```java
    Item findItem = em.find(Item.class, movie.getId());
    ```
    

### 전략 장단점

| 전략 | 장점 | 단점 |
| --- | --- | --- |
| 조인 | 테이블 정규화, 저장공간 효율화 | 조회 시 조인이 많아질 수 있음, 저장할 때 insert 2번 나감, 복잡함 |
| 단일 테이블 | 조인이 필요x, 쿼리 단순 | 모든 자식 컬럼은 null 허용해야함, 테이블이 커져서 성능이 느려질 수 있음 |
| 구현 클래스 |  | 쓰지 말자^^ |

**결론: 조인 전략과 단일 테이블 전략의 트레이드 오프를 비교해서 둘 중에 하나로 결정하자**

### @Mapped Superclass

- 공통으로 쓰는 컬럼을 설정하는 클래스에 어노테이션을 붙이고 하위 클래스에서 상속을 받으면 DB에 각각 테이블에 컬럼이 생성됨
## 상속 및 MappedSuperclass 예제

> 예제 - jpa-ex-0513_1-jpaShop
> 

### 예제 진행 중 오류 해결

```java
// 오류
@ManyToMany(mappedBy = "items")
private ArrayList<Category> categories = new ArrayList<>();

// 해결법
private List<Category> categories = new ArrayList<>();
```

- 이전 예제 진행 중 위의 코드처럼 Entity안에 해당 필드를 ArrayList로 했더니 타입 오류가 나왔다.
- ArrayList를 List로 바꾸었더니 해결되었다.
- 이유 : ??

## 프록시

```java
Member findMember = em.getReference(Member.class, member.getId());
```

- em.find() 대신 em.getReference()를 사용하면 데이터베이스 조회를 미루는 가짜(프록시) 엔티티 객체를 조회한다.
- 실제로 DB에 쿼리를 던지는 타이밍은 해당 객체를 사용할 때 쿼리를 던진다.

### 프록시 특징

- 프록시 흐름
    1. 아직 쿼리를 조회하지않은 상태같이 프록시 안에 값이 없을 때 해당 객체를 사용하면 **영속성 컨텍스트**에게 초기화를 요청한다.
    2. 영속성 컨텍스트는 DB를 조회한다.
    3. 조회된 값을 실제 entity로 생성해서 프록시는 해당 entity에서 값을 빼서 쓴다.
- 프록시 객체는 처음 사용할 때 한 번만 초기화된다.
- 프록시 객체가 실제로 엔티티로 바뀌는 것이 아님! 프록시 객체를 통해 실제 엔티티에 접근하는 것!
- 프록시 객체의 타입 체크할 때는 == 으로 하면 안되고 instance of 로 비교해야함
- 같은 트랜젝션에서 영속성 컨텍스트에 있는 동일한 객체는 항상 ==을 만족해야한다.
    - 영속성 컨텍스트에 찾는 엔티티가 이미 있으면 em.getReference()를 사용했어도 프록시 객체가 아닌 실제 엔티티가 반환됨
    - 프록시로 한번 조회하면 em.find로 조회했어도 프록시 객체를 반환함
- 영속성 컨텍스트의 도움을 받을 수 없는 준영속 상태일 때 프록시를 초기화하려고 하면 오류가 나온다.
    
    ```java
    Member refMember = em.getReference(Member.class, 1L);
    
    // 영속성 컨텍스트를 준영속 상태로 만듬
    1. em.close();
    2. em.clear();
    3. em.detach(refMember);
    
    refMember.getUsername(); // 오류남
    ```

## 지연로딩 즉시로딩

### 지연로딩 - Lazy를 사용하여 프록시를 조회

```java
@Entity
public class Member {
	....
	
	@ManyToOne(fetch = FatchType.LAZY) <<
	@JonColumn(name = 'TEAM_ID')
	private Team team;
}
```

- Member를 em.find()하면 Team을 Join하지 않고 getTeam().getName()을 했을 때 쿼리를 던진다.

### 즉시로딩 - EAGER를 사용하여 함께 조회

```java
@Entity
public class Member {
	....
	
	@ManyToOne(fetch = FatchType.EAGER) <<
	@JonColumn(name = 'TEAM_ID')
	private Team team;
}
```

- Member를 em,find()할 때 join을 사용하여 함께 가져오기 때문에 프록시 객체가 아님

### 프록시와 즉시로딩 주의

- 실무에서는 가급적 **지연 로딩**만 사용해라 - 연관관계가 여러개가 있다면 Member를 조회할 때 모든 연관 테이블을 조인하여 조회함(성능 이슈)
- 즉시로딩을 사용하면 JPQL을 사용할 때 N + 1 문제를 일으킨다. ex) Member 목록을 조회할 때 회원의 수만큼 연관된 관계의 테이블을 일일이 조회한다.
- @ManyToOne, @OneToOne 은 기본이 즉시로딩 / @OneToMany, @ManyToMany는 기본이 지연 로딩

> **결론: 실무에서는 모든 연관관계는 지연로딩으로 만들자**
> 

### 영속성 전이(CASCADE)

```java
@ManyToOne(CascadeType.All)
```

- 하나의 엔티티를 persist()할 때 연관된 entity도 같이 관리하고 싶을 때 사용 ex) parent 엔티티를 등록할 때 포함된 children도 같이 등록되게 만들 때
- 영속성 전이는 연관관계를 매핑하는 것과 아무 관련없음. 그냥 엔티티를 영속화 할 때 연관된 엔티티도 함께 영속화하는 편리함을 제공
- 종류
    1. All : 모든 CRUD
    2. PERSIST : 등록/수정 시
    3. REMOVE : 삭제 시

> **결론: 자식 엔티티가 다른 것과 연관되어있지 않고 딱 하나의 부모에 대해서만 연관되어 있을 때만 CASCADE를 사용하라!** ex) 게시판 - 첨부파일 관계
> 

### 고아 객체

```java
@OneToMany(mappedBy = '', orphanRemuval = true)
```

- 부모 객체와 연결이 끊기면 자식 객체를 지우는 기능
- **참조하는 곳이 하나일 때만 사용해야함, 특정 엔티티가 개인 소유할 때 사용**
- OneToOne, OneToMany만 사용가능
- 부모 객체를 제거하면 자식객체도 자동으로 삭제된다.

### 영속성 전이 + 고아 객체 : 생명 주기 관리

- CascadeType.ALL + orphanRemuval = true로 설정시
    - 부모 엔티티가 자식 엔티티의 생명주기를 관리할 수 있다. → 자식 엔티티는 dao, repository를 만들지 않아도 됨
    - 도메인 주도 설계(DDD)의 Aggregate Root개념을 구현할 때 유용
        
        *Aggregate - 어떤 데이터를 하나의 논리적인 개념으로 묶는 패턴 ex) 게시판 - root / 게시판

## JPA 값 타입

### 엔티티 타입

- @Entity로 정의하는 객체
- 데이터가 변해도(엔티티 안에 컬럼이 모두 변해도) 식별자로 인식이 가능 따라서 **추적이 가능**

### 값 타입

- int, Integer, String처럼 단순히 값으로 사용하는 자바 기본 타입이나 객체
- 식별자가 없고 값만 있기 때문에 **추적 불가**

### 값 타입 분류

1. 기본값 타입 - 자바 기본 타입(int, double), 래퍼 클래스(Integer, Long), String
    - 생명주기를 엔티티에 의존함 - 엔티티를 삭제하면 같이 삭제
    - 값 타입은 공유하면 x (ex. 회원 이름 변경 시 다른 회원의 이름도 함께 변경되면 안됨)
2. **임베디드 타입**(embedded type, 복합 값 타입)
    - 새로운 값 타입을 직접 정의할 수 있다.
    - 주로 기본 값 타입을 모아서 만들어서 **복합 값 타입**이라고함
    - int, String과 같은 값타입(엔티티 타입이 아님 따라서 값이 변경되면 끝)
    - ex) 회원은 이름, 근무 기간, 집 주소를 가진다 → 이름 = name, **근무기간(startDt, endDt), 집 주소(시, 도, 등등)**
    - 임베디드 타입과 테이블 매핑
        - 객체와 테이블을 아주 세밀하게 매핑하는 것이 가능
3. 컬렉션 값 타입(자바 컬렉션처럼 기본값이나 임베디드 타입을 넣는 리스트 타입)

### 임베디드 타입

1. @AttributeOverride: 속성 재정의
    - 한 엔티티에서 같은 값 타입을 사용하면? → 컬럼 명이 중복됨
    - 따라서 @AttributeOverrides, @AttributeOverride를 사용해서 컬럼 명 속성을 재정의 한다.
    
    ```java
     //Member.class
     
    @Embedded
    private Address homeAddress;
    
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "city",
                    column =@Column(name = "WORK_CITY")),
            @AttributeOverride(name = "street",
                    column = @Column(name = "WORK_STREET")),
            @AttributeOverride(name = "zipcode",
                    column = @Column(name = "WORK_ZIPCODE"))
    })
    private Address workAddress;
    ```
    
2. 임베디드 타입의 값이 null이면 안의 모든 컬럼의 값은 null이다.

### 값 타입과 불변 객체

- 값 타입은 자바에서 레퍼런스 변수로써 사용하기 때문에 값타입의 값을 변경하면 같은 변수를 사용하는 모든 곳이 변경됨
    
    ```java
    Address adrs = new Address(...생성 시 값 셋팅);
    
    member1.setAdrs(adrs);
    member2.setAdrs(adrs);
    
    member1.getAdrs().setCity("newCity");
    
    // 이후 JPA에서 member1, member2 모두 newCity로 업데이트 쿼리가 실행된다.
    ```
    
- 따라서 값 타입은 불변 객체로 만들어야한다. → 값타입에 setter를 없앤다.
    
    ```java
    // Address.class
    Class Address {
    	
    	String city;
    	
    	....
    	
    	
    	public getCity() {
    		....
    	}
    	
    	// setter는 만들지 않거나 private으로 설정해서 밖에서 변경하는것을 막자
    	private setCity() {}
    
    }
    ```
    
- 따라서 값타입을 변경하고 싶다면 새로 만들어서 값타입 자체를 교체하는 식으로 수정해야한다.
    
    ```java
    Address adrs = new Address(...생성 시 값 셋팅);
    
    member1.setAdrs(adrs);
    member2.setAdrs(adrs);
    
    Address changedAdrs = new Address(...생성 시 값 셋팅, "newCity");
    member1.setAdrs(changedAdrs);
    ```
    

### 값 타입 비교

- 값 타입을 비교 시 ==이 아닌 equal() 로 비교해야한다.
- 따라서 Object의 equal()을 쓰는것이 아닌 값타입에 override해서 사용한다. + 오버라이드하면서 해시코드도 같이 오버라이드 해야함

## 값 타입 컬렉션

- 값 타입을 엔티티에 하나 이상을 저장할 때 사용(회원 - 좋아하는 음식, 이전 주소들 등등)
- @ElementCollection, @CollectionTable을 사용해 DB테이블에 맵핑 시킨다.
    
    ```java
    // Member.class
    public class Member {
    
    		......
    		
        @ElementCollection
        @CollectionTable(name = "FAVORITE_FOOD", joinColumns =
            @JoinColumn(name = "MEMBER_ID")
        )
        @Column(name = "FOOD_NM")
        private Set<String> favoriteFoods = new HashSet<>();
    
        @ElementCollection
        @CollectionTable(name = "ADDRESS", joinColumns =
            @JoinColumn(name = "MEMBER_ID")
        )
        private List<Address> addressHistory = new ArrayList<>();
    }
    ```
    
- 엔티티 안의 값타입 컬렉션도 엔티티의 생명주기에 따라서 관리된다. → 따라서 영속성 전이(CASECADE) + 고아 객체 제거 기능을 필수로 가지고 있다.
- 값 타입 컬렉션은 지연로딩이다.

### 값 타입 컬렉션 예시

- 값 타입 컬렉션 안의 값 수정
    
    ```java
    Member m = new Member();
    m.getAddressHistory().add(new Address("city1", "100"));
    m.getAddressHistory().add(new Address("city2", "100"));
    
    // 값 넣은 후 
    ---------
    
    // 값 수정
    m.getAddressHistory().remove(new Address("city1", "100")); // 이때 값타입에 equals(), hashCode()가 제대로 정의되어있어야지 제대로 기능을 함!!
    m.getAddressHistory().add(new Address("newCity", "100"));
    
    ```
    
    - 하지만 실제 실행되는 쿼리를 보면 컬렉션의 값을 수정할 때 해당 사용자에 매핑되어있는 모든 주소 정보를 삭제한 후 다시 넣는 쿼리가 실행된다. → **값 타입 컬렉션에 제약사항이 있다**

### 값 타입 컬렉션의 제약사항

- 값 타입은 엔티티와 다르게 식별자가 없다.
- 값은 변경하면 추적하기 어렵다.
- 값 타입 컬렉션을 변경하면 해당되는 엔티티와 관련된 컬렉션 모두 삭제하고 다시 넣는다.
- **따라서 값타입 컬렉션을 매핑하는 테이블은 모든 컬럼을 묶어서 기본키로 구성해야함 → null x, 중복 데이터 x**

### 값 타입 컬렉션의 대안

- 실무에서는 값 타입 컬렉션을 쓰지않고 일대다 관계를 고려 → 값 타입을 엔티티로 **승급**시킨다.
    
    ```java
    // 주소 엔티티를 만듬
    @Entity
    @TABLE(name = "ADDRESS")
    class AddressEntity {
    		
    		@Id @GeneratedValue
    		private Long id;
    		
    		private Addres address;
    		
    		... getter, setter
    }
    
    // Member.class
    class Member {
    
    	....
    
      // 값 타입 컬렉션으로 매핑하는 것 대신 일대다 관계로 엔티티로써 매핑
      //@ElementCollection
      //@CollectionTable(name = "ADDRESS", joinColumns =
      //    @JoinColumn(name = "MEMBER_ID")
      //)
      @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
      @JoinColumn(name = "MEMBER_ID")
      private List<Address> addressHistory = new ArrayList<>();
      
    }
    ```
    

### 값 타입 컬렉션으로만 쓰는 기준

- 정말 단순한 데이터일 때 값타입 컬렉션일때(ex. 사용자의 정보 중 체크박스로 멀티 체크를 하는 정보)

## 객체지향 쿼리 언어

- JPA를 사용하면 엔티티 객체를 중심으로 개발해야한다.
- 문제는 검색 쿼리 → 필요한 데이터만 DB에서 불러오려면 결국 검색 조건이 포함된 SQL이 필요

### JPQL

- JPQL은 엔티티 객체를 대상으로 쿼리 / SQL은 데이터베이스 테이블을 대상으로 쿼리

## JPQL

### TypeQuery, Query

- TypeQuery - 반환값이 명확할 때 사용함
    
    ```java
    TypeQuery<Member> query = em.createQuery("SELECT m FORM Member AS m", Member.class);
    ```
    
- Query - 반환값이 명확하지 않을 때 사용함
    - 
    
    ```java
    Query<Member> query = em.createQuery("SELECT m.username, m.age FORM Member AS m");
    ```
    

### 결과 조회

- getResultList() - 결과가 하나 이상일 때 리스트 반환, 값이 없으면 빈 리스트 반환
- getSingleResult() - 결과가 무조건 하나일때 사용, 값이 없거나 2개 이상이면 exception 반환

### 파라미터 바인딩

- 이름 기준
    
    ```java
    Member findMember = em.createQuery("SELECT m FROM Member as m WHERE username = :username", Member.class)
    	.setParameter("username", "hi");
    	.getSingleResult()
    ```
    
- 위치 기준 - 쓰지마

### 프로젝션

- select절에서 조회할 대상을 정하는 것
- 엔티티, 임베디드 타입, 스칼라 타입(숫자, 문자 등 기본 타입)을 지정할 수 있다.
- 스칼라 타입으로 조회 시 방법
    - Query타입으로 조회
    - Object[]로 조회
    - **new 명령어로 조회 - 클래스 경로를 모두 적어야하는 단점이 있음(나중에 해결할 방법 있음)**
        
        ```java
        // new 명령어로 스칼라 타입 조회
        List<MemberDTO> result = em.createQuery("SELECT new org.example.jpa.jpql.MemberDTO(m.username, m.age) FROM Member m")
        	    .getResultList();
        ```
        

### 페이징

```java
List<Member> result = em.createQuery("SELECT m FROM Member m order by m.age desc", Member.class)
        .setFirstResult(1)
        .setMaxResults(10)
        .getResultList();
```

### 서브쿼리

- JPQL에서 FROM절에는 서브쿼리를 사용할 수 없다!!
- 그래서 다른 방식으로 풀어야함 (조인 or 네이티브 쿼리 or 쿼리 나누기)

### JPQL 함수

- 사용자 정의 함수 - 사용자가 정의한 함수를 JPQL에서 쓸때는 미리 사용자 함수를 등록하고 사용해야 한다. (사용법은 찾아서 쓰기)

### JPQL 경로 표현식

- 점을 찍어 객체 그래프를 탐색하는 것

```sql
select m.username -- 상태 필드
from Member m 
join m.team t -- 단일 값 연관 필드
join m.orders o  -- 컬렉션 값 연관 필드
where t.team = '팀A'
```

1. 상태 필드 
    - 단순히 값을 저장하기 위한 필드
    - 경로 탐색의 끝, 탐색x
2. 단일 값 연관 필드 
    - @ManyToOne, @OneToOne → 대상이 엔티티
    - 묵시적 내부 조인(inner join) 발생, 탐색 o
        
        ```sql
        // jpql
        String query = "select m.team from Member m"
        
        // >> sql 묵시적 내부 조인
        SELECT t.name, t.id
        FROM   MEMBER m
        JOIN   TEAM t
        ON     m.member_id = t.member_id
        ```
        
3. 컬렉션 값 연관 필드 
    - @OneToMany, @ManyToMany → 대상이 컬렉션
    - 묵시적 내부 조인 발생, 탐색x
        
        ```sql
        // jpql -> 묵지적 내부 조인, t.members.name < 이렇게 탐색할 수 없음
        String query = "select t.members from Team t"
        
        // 명시적으로 조인하여 컬렉션에서 탐색해야함
        String query = "select m.name from Team t join t.memebers m;
        ```
        

> **결론 : 묵시적 조인을 쓰지 말고 명시적 조인을 사용하여 실제 sql이 나가는 형식과 비슷하게 jpql을 만들어라**
> 

### 페치 조인(fecth join)

- jpql에서 성능 최적화를 위한 join 종류
- 엔티티에 매핑되어있는 단일 객체 조회 시 join fetch를 하면 같이 값을 가지고 옴
    
    ```sql
    String query = "select m from Member m join fetch m.team";
    ```
    
- 엔티티에 매핑되어있는 컬렉션을 조회 시 join fetch를 하면 값을 가지고옴
    
    ```sql
    String query = "select t from Team t join fetch t.members";
    // -> 하지만 sql특성상 값이 뻥튀기 되어서 같은 엔티티가 중복되어서 나옴
    // ex) 1팀 안에 2명이 있다면 해당 쿼리를 날렸을 때 1팀에 2명이기 때문에 2개의 row가 나와서 list에 2개가 담겨서 나옴
    
    // 따라서 distinct를 사용해서 뽑는다.
    String query = "select distinct t from Team t join fetch t.members";
    ```
    
    - jpa에서 distinct를 사용 시 2개의 기능을 수행함
        1. sql의 distinct 기능
        2. 엔티티가 중복될 때 중복된 엔티티 제거
- 페치조인과 일반조인의 차이
    - 일반 조인은 조인을 해도 조인된 엔티티를 함께 조회하지 않는다 → 따라서 해당 엔티티를 사용할때 (ex. team.getMembers();) 쿼리를 한번 더 날림

### 페치 조인의 특징과 한계

- 페치 조인 대상에는 별칭을 줄 수 없다. → 페치 조인 대상은 where절에 사용할 수 없다. 따라서 페치 조인을 사용하면 해당 엔티티와 관련되어 있는 대상을 **모두** 조회하는 식으로 써야한다.
- 둘 이상의 컬렉션은 페치조인 할 수 없다.
- 컬렉션 페치 조인을 하면 페이징 API를 사용할 수 없다. → 사용하는 방법은 batch size 설정하는 법이 있음 나중에 찾아보자
- 실무에서는 글로벌 로딩 전략을 모두 지연 로딩으로 셋팅하고 최적화가 필요한 곳에 페치 조인을 적용하자

> **결론 : 여러 테이블을 조인하여 엔티티가 아닌 다른 모양으로 결과를 내야하면 일반 조인을 사용하여 DTO로 반환하는 것이 효과적이다**
> 

## 엔티티 직접 사용

- 기본 키 값
    
    ```sql
    // jpql
    "select count(m) from Member m"
    
    // sql 
    SELECT COUNT(m.id) FROM MEMBER m;
    ```
    
    - JPQL에서 엔티티를 직접 사용하면 SQL에서 해당 엔티티의 기본 키 값을 사용한다.
- 외래 키 값
    
    ```sql
    "select m from Member m where m.team = :team"
    ```
    
    - 매핑된 엔티티를 그대로 넣으면 해당 엔티티의 외래키(ex. TEAM_ID)가 그대로 들어간다.

## 벌크 연산

- JPA에서 특정 엔티티를 update, delete하는 것 말고 여러개의 데이터를 갱신,삭제하는 기능
- .excuteUpdate()
- 주의점
    - 해당 기능은 영속성 컨텍스트에 담지않고 바로 db에 갱신하기 때문에 다른 영속성 컨텍스트에 담긴 데이터와 꼬일 수 있다. 2가지 방법으로 해결 가능
    1. 벌크 연산을 먼저 실행
    2. 벌크 연산을 수행 후 영속성 컨텍스트 초기화